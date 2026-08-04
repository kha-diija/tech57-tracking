import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  ViewChild,
  inject,
  signal,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { LucideAngularModule, Fuel, MapPin, Route, TrendingUp, Send, Search, X } from 'lucide-angular';
import { Subject, debounceTime, distinctUntilChanged, switchMap, of, catchError } from 'rxjs';
import { Etablissement } from '../../shared/models/etablissement.model';
import { EtablissementService } from '../../shared/services/etablissement.service';
import {
  PointTrajet,
  SimulateurTrajetResult,
  SimulateurTrajetService,
  TypeRoute,
} from './simulateur-trajet.service';

interface Suggestion {
  source: 'etablissement' | 'adresse';
  label: string;
  sousLabel: string;
  point: PointTrajet;
}

type Cible = 'origine' | 'destination';

@Component({
  selector: 'app-simulateur-trajet',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './simulateur-trajet.html',
  styleUrl: './simulateur-trajet.scss',
})
export class SimulateurTrajet implements AfterViewInit, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  icons = { Fuel, MapPin, Route, TrendingUp, Send, Search, X };

  private readonly etablissementService = inject(EtablissementService);
  etablissements: Etablissement[] = [];

  texteOrigine = signal('');
  texteDestination = signal('');
  origine = signal<PointTrajet | null>(null);
  destination = signal<PointTrajet | null>(null);

  suggestionsOrigine = signal<Suggestion[]>([]);
  suggestionsDestination = signal<Suggestion[]>([]);
  cibleActive = signal<Cible | null>(null);
  rechercheEnCours = signal(false);

  typeRoute = signal<TypeRoute>('Autoroute');
  prixCarburant = signal<number>(11.5);

  resultat = signal<SimulateurTrajetResult | null>(null);
  comparatif = signal<SimulateurTrajetResult[]>([]);
  enChargement = signal(false);
  erreur = signal<string | null>(null);

  private map!: L.Map;
  private markerOrigine: L.Marker | null = null;
  private markerDestination: L.Marker | null = null;
  private routeLayer = L.layerGroup();
  private rechercheSubject = new Subject<{ texte: string; cible: Cible }>();

  /** Observer de taille du conteneur + handler de resize nommés pour pouvoir les nettoyer proprement. */
  private resizeObserver: ResizeObserver | null = null;
  private readonly onWindowResize = () => this.map?.invalidateSize();

  private readonly iconOrigine = this.creerIcone('#10b981');
  private readonly iconDestination = this.creerIcone('#ff4500');

  constructor(private http: HttpClient, private trajetService: SimulateurTrajetService) {
    this.rechercheSubject
      .pipe(
        debounceTime(350),
        distinctUntilChanged((a, b) => a.texte === b.texte && a.cible === b.cible),
        switchMap(({ texte, cible }) => {
          if (texte.trim().length < 3) return of({ cible, suggestions: [] as Suggestion[] });
          this.rechercheEnCours.set(true);
          return this.rechercherAdresses(texte).pipe(
            switchMap((adresses) => of({ cible, suggestions: [...this.filtrerEtablissements(texte), ...adresses] })),
            catchError(() => of({ cible, suggestions: this.filtrerEtablissements(texte) }))
          );
        })
      )
      .subscribe(({ cible, suggestions }) => {
        this.rechercheEnCours.set(false);
        if (cible === 'origine') this.suggestionsOrigine.set(suggestions);
        else this.suggestionsDestination.set(suggestions);
      });
  }

  ngAfterViewInit(): void {
    this.initMap();
    this.chargerEtablissements();
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.onWindowResize);
    this.resizeObserver?.disconnect();
    this.resizeObserver = null;
    this.map?.remove();
  }

  private initMap(): void {
    this.map = L.map(this.mapContainer.nativeElement, { zoomControl: true }).setView([31.7917, -7.0926], 6);

    L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; OpenStreetMap &copy; CARTO',
      maxZoom: 19,
    }).addTo(this.map);

    this.routeLayer.addTo(this.map);

    // Corrige l'affichage cassé (tuiles grises) au premier rendu dans un conteneur flex/absolu.
    // ResizeObserver réagit à tout changement de taille du conteneur (panneau qui apparaît,
    // animation, sidebar qui se replie...), pas seulement au resize de la fenêtre.
    requestAnimationFrame(() => this.map.invalidateSize());

    this.resizeObserver = new ResizeObserver(() => this.map?.invalidateSize());
    this.resizeObserver.observe(this.mapContainer.nativeElement);

    window.addEventListener('resize', this.onWindowResize);
  }

  private creerIcone(couleur: string): L.DivIcon {
    return L.divIcon({
      className: 'trajet-marker',
      html: `<span style="
        display:block;width:16px;height:16px;border-radius:50% 50% 50% 0;
        background:${couleur};border:2px solid #fff;
        box-shadow:0 2px 8px rgba(0,0,0,.4);
        transform:rotate(-45deg);"></span>`,
      iconSize: [16, 16],
      iconAnchor: [8, 16],
    });
  }

  private chargerEtablissements(): void {
    // Réutilise le vrai service (celui qui alimente déjà l'onglet Établissements) —
    // même endpoint /api/admin/etablissements, même modèle, pas de duplication.
    this.etablissementService.getAll().subscribe({
      next: (data) => (this.etablissements = data),
      error: () => this.erreur.set('Impossible de charger la liste des établissements.'),
    });
  }

  private filtrerEtablissements(texte: string): Suggestion[] {
    const t = texte.toLowerCase();
    return this.etablissements
      .filter((e) => e.designation.toLowerCase().includes(t) || e.reference.toLowerCase().includes(t))
      .slice(0, 4)
      .map((e) => {
        const [lat, lng] = (e.localisationGps ?? '').split(',').map((v) => parseFloat(v.trim()));
        return {
          source: 'etablissement' as const,
          label: e.designation,
          sousLabel: e.reference,
          point: { idEtablissement: e.idEtablissement, nom: e.designation, lat, lng },
        };
      })
      .filter((s) => isFinite(s.point.lat) && isFinite(s.point.lng));
  }

  /** Recherche libre d'adresse (type Waze) via Nominatim — non limitée aux établissements. */
  private rechercherAdresses(texte: string) {
    const url = `https://nominatim.openstreetmap.org/search?format=json&limit=5&countrycodes=ma&q=${encodeURIComponent(texte)}`;
    return this.http.get<any[]>(url).pipe(
      switchMap((resultats) =>
        of(
          resultats.map((r) => ({
            source: 'adresse' as const,
            label: r.display_name.split(',')[0],
            sousLabel: r.display_name,
            point: { idEtablissement: null, nom: r.display_name.split(',')[0], lat: parseFloat(r.lat), lng: parseFloat(r.lon) },
          }))
        )
      )
    );
  }

  onSaisie(texte: string, cible: Cible): void {
    if (cible === 'origine') this.texteOrigine.set(texte);
    else this.texteDestination.set(texte);
    this.cibleActive.set(cible);
    this.rechercheSubject.next({ texte, cible });
  }

  choisirSuggestion(suggestion: Suggestion, cible: Cible): void {
    if (cible === 'origine') {
      this.origine.set(suggestion.point);
      this.texteOrigine.set(suggestion.label);
      this.suggestionsOrigine.set([]);
      this.placerMarqueur('origine', suggestion.point);
    } else {
      this.destination.set(suggestion.point);
      this.texteDestination.set(suggestion.label);
      this.suggestionsDestination.set([]);
      this.placerMarqueur('destination', suggestion.point);
    }
    this.cibleActive.set(null);
  }

  effacer(cible: Cible): void {
    if (cible === 'origine') {
      this.origine.set(null);
      this.texteOrigine.set('');
      this.markerOrigine?.remove();
      this.markerOrigine = null;
    } else {
      this.destination.set(null);
      this.texteDestination.set('');
      this.markerDestination?.remove();
      this.markerDestination = null;
    }
    this.routeLayer.clearLayers();
    this.resultat.set(null);
  }

  private placerMarqueur(cible: Cible, point: PointTrajet): void {
    if (cible === 'origine') {
      this.markerOrigine?.remove();
      this.markerOrigine = L.marker([point.lat, point.lng], { icon: this.iconOrigine }).addTo(this.map);
    } else {
      this.markerDestination?.remove();
      this.markerDestination = L.marker([point.lat, point.lng], { icon: this.iconDestination }).addTo(this.map);
    }
    if (this.markerOrigine && this.markerDestination) {
      const bounds = L.latLngBounds(this.markerOrigine.getLatLng(), this.markerDestination.getLatLng());
      this.map.fitBounds(bounds, { padding: [60, 60] });
    } else {
      this.map.flyTo([point.lat, point.lng], 13);
    }
  }

  calculer(): void {
    if (!this.peutCalculer()) return;
    this.enChargement.set(true);
    this.erreur.set(null);
    this.comparatif.set([]);

    this.trajetService
      .calculer({
        origine: this.origine()!,
        destination: this.destination()!,
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
        origine: this.origine()!,
        destination: this.destination()!,
        typeRoute: this.typeRoute(),
        prixCarburantLitre: this.prixCarburant(),
      })
      .subscribe({
        next: (res) => {
          this.comparatif.set(res);
          this.resultat.set(res[0]);
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
        opacity: 0.92,
        lineCap: 'round',
      }).addTo(this.routeLayer);
      this.map.fitBounds(ligne.getBounds(), { padding: [50, 50] });
    }
  }

  peutCalculer(): boolean {
    const o = this.origine();
    const d = this.destination();
    return !!o && !!d && (o.lat !== d.lat || o.lng !== d.lng) && this.prixCarburant() > 0;
  }
}