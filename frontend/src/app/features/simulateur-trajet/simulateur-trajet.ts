import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { LucideAngularModule, Fuel, MapPin, Route, TrendingUp, Send } from 'lucide-angular';
import { environment } from '../../../environments/environment';
import {
  EtablissementOption,
  SimulateurTrajetResult,
  SimulateurTrajetService,
  TypeRoute,
} from './simulateur-trajet.service';

@Component({
  selector: 'app-simulateur-trajet',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './simulateur-trajet.html',
  styleUrl: './simulateur-trajet.scss',
})
export class SimulateurTrajet implements AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  icons = { Fuel, MapPin, Route, TrendingUp, Send };

  etablissements = signal<EtablissementOption[]>([]);
  idOrigine = signal<number | null>(null);
  idDestination = signal<number | null>(null);
  typeRoute = signal<TypeRoute>('Autoroute');
  prixCarburant = signal<number>(11.5); // DH/L, éditable par l'utilisateur

  resultat = signal<SimulateurTrajetResult | null>(null);
  comparatif = signal<SimulateurTrajetResult[]>([]);
  enChargement = signal(false);
  erreur = signal<string | null>(null);

  private map!: L.Map;
  private markersLayer = L.layerGroup();
  private routeLayer = L.layerGroup();

  constructor(private http: HttpClient, private trajetService: SimulateurTrajetService) {}

  ngAfterViewInit(): void {
    this.initMap();
    this.chargerEtablissements();
  }

  ngOnDestroy(): void {
    this.map?.remove();
  }

  private initMap(): void {
    // Centré sur le Maroc par défaut
    this.map = L.map(this.mapContainer.nativeElement, {
      zoomControl: true,
    }).setView([31.7917, -7.0926], 6);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OpenStreetMap &copy; CARTO',
      maxZoom: 19,
    }).addTo(this.map);

    this.markersLayer.addTo(this.map);
    this.routeLayer.addTo(this.map);
  }

  private chargerEtablissements(): void {
    this.http
      .get<EtablissementOption[]>(`${environment.apiUrl}/etablissements`)
      .subscribe({
        next: (data) => {
          this.etablissements.set(data);
          this.tracerMarqueursEtablissements(data);
        },
        error: () => this.erreur.set("Impossible de charger la liste des établissements."),
      });
  }

  private tracerMarqueursEtablissements(liste: EtablissementOption[]): void {
    for (const etab of liste) {
      const [lat, lng] = (etab.localisationGps ?? '').split(',').map((v) => parseFloat(v.trim()));
      if (!isFinite(lat) || !isFinite(lng)) continue;
      L.circleMarker([lat, lng], {
        radius: 5,
        color: '#e85002',
        fillColor: '#ff4500',
        fillOpacity: 0.85,
        weight: 1.5,
      })
        .bindTooltip(etab.designation, { direction: 'top' })
        .addTo(this.markersLayer);
    }
  }

  calculer(): void {
    if (!this.peutCalculer()) return;
    this.enChargement.set(true);
    this.erreur.set(null);
    this.comparatif.set([]);

    this.trajetService
      .calculer({
        idEtablissementOrigine: this.idOrigine()!,
        idEtablissementDestination: this.idDestination()!,
        typeRoute: this.typeRoute(),
        prixCarburantLitre: this.prixCarburant(),
      })
      .subscribe({
        next: (res) => {
          this.resultat.set(res);
          this.tracerItineraire(res);
          this.enChargement.set(false);
        },
        error: () => {
          this.erreur.set("Le calcul d'itinéraire a échoué. Réessayez.");
          this.enChargement.set(false);
        },
      });
  }

  comparerItineraires(): void {
    if (!this.peutCalculer()) return;
    this.enChargement.set(true);
    this.erreur.set(null);

    this.trajetService
      .comparerItineraires({
        idEtablissementOrigine: this.idOrigine()!,
        idEtablissementDestination: this.idDestination()!,
        typeRoute: this.typeRoute(),
        prixCarburantLitre: this.prixCarburant(),
      })
      .subscribe({
        next: (res) => {
          this.comparatif.set(res);
          this.resultat.set(res[0]); // la moins chère en premier (triée côté backend)
          this.tracerItineraire(res[0]);
          this.enChargement.set(false);
        },
        error: () => {
          this.erreur.set('La comparaison des itinéraires a échoué.');
          this.enChargement.set(false);
        },
      });
  }

  proposerBudget(idMission: number): void {
    const sim = this.resultat();
    if (!sim) return;
    this.trajetService.proposerBudget(sim.idSimulation, idMission).subscribe({
      next: (res) => this.resultat.set(res),
      error: () => this.erreur.set("Impossible d'associer ce budget à la mission."),
    });
  }

  private tracerItineraire(res: SimulateurTrajetResult): void {
    this.routeLayer.clearLayers();

    if (res.pointsRoute?.length) {
      const ligne = L.polyline(res.pointsRoute as L.LatLngExpression[], {
        color: '#ff4500',
        weight: 5,
        opacity: 0.9,
      }).addTo(this.routeLayer);
      this.map.fitBounds(ligne.getBounds(), { padding: [40, 40] });
    } else {
      const bounds = L.latLngBounds(
        [res.latOrigine, res.lngOrigine],
        [res.latDestination, res.lngDestination]
      );
      this.map.fitBounds(bounds, { padding: [40, 40] });
    }

    L.marker([res.latOrigine, res.lngOrigine]).addTo(this.routeLayer).bindTooltip('Départ');
    L.marker([res.latDestination, res.lngDestination]).addTo(this.routeLayer).bindTooltip('Arrivée');
  }

  peutCalculer(): boolean {
    return (
      !!this.idOrigine() &&
      !!this.idDestination() &&
      this.idOrigine() !== this.idDestination() &&
      this.prixCarburant() > 0
    );
  }
}