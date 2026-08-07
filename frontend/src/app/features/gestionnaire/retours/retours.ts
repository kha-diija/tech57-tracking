import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  PackageOpen,
  User,
  ClipboardCheck,
  AlertTriangle,
  RefreshCw,
  X,
} from 'lucide-angular';
import { RetourMaterielGestionService } from './services/retour-materiel.service';
import { SortieARegulariserDto } from './models/retour.model';

interface LigneVentilation {
  idMateriel: number;
  materielNom: string;
  materielReference: string;
  quantiteSortie: number;
  quantiteBonEtat: number;
  quantiteEnPanne: number;
}

@Component({
  selector: 'app-retours-stock',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './retours.html',
  styleUrl: './retours.scss',
})
export class RetoursStock implements OnInit {
  private retourService = inject(RetourMaterielGestionService);

  readonly icons = { PackageOpen, User, ClipboardCheck, AlertTriangle, RefreshCw, X };

  sorties = signal<SortieARegulariserDto[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  showModal = signal(false);
  sortieEnCours: SortieARegulariserDto | null = null;
  lignesVentilation: LigneVentilation[] = [];
  saving = signal(false);
  modalError = signal<string | null>(null);

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.retourService.listerARegulariser().subscribe({
      next: (res) => { this.sorties.set(res); this.loading.set(false); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Erreur lors du chargement des retours à traiter');
        this.loading.set(false);
      },
    });
  }

  ouvrirVentilation(s: SortieARegulariserDto): void {
    this.sortieEnCours = s;
    this.lignesVentilation = s.lignes.map((l) => ({
      idMateriel: l.idMateriel,
      materielNom: l.materielNom,
      materielReference: l.materielReference,
      quantiteSortie: l.quantiteSortie,
      quantiteBonEtat: l.quantiteSortie,
      quantiteEnPanne: 0,
    }));
    this.modalError.set(null);
    this.showModal.set(true);
  }

  fermerModal(): void {
    this.showModal.set(false);
    this.sortieEnCours = null;
  }

  totalLigne(l: LigneVentilation): number {
    return (Number(l.quantiteBonEtat) || 0) + (Number(l.quantiteEnPanne) || 0);
  }

  ligneValide(l: LigneVentilation): boolean {
    return this.totalLigne(l) === l.quantiteSortie;
  }

  toutValide(): boolean {
    return this.lignesVentilation.every((l) => this.ligneValide(l));
  }

  confirmerVentilation(): void {
    if (!this.sortieEnCours) return;
    if (!this.toutValide()) {
      this.modalError.set('Pour chaque matériel, la somme (bon état + en panne) doit égaler la quantité sortie.');
      return;
    }
    this.saving.set(true);
    this.modalError.set(null);
    const payload = {
      lignes: this.lignesVentilation.map((l) => ({
        idMateriel: l.idMateriel,
        quantiteBonEtat: Number(l.quantiteBonEtat) || 0,
        quantiteEnPanne: Number(l.quantiteEnPanne) || 0,
      })),
    };
    this.retourService.validerRetour(this.sortieEnCours.idSortie, payload).subscribe({
      next: () => {
        this.saving.set(false);
        this.fermerModal();
        this.charger();
      },
      error: (err) => {
        this.modalError.set(err?.error?.message ?? 'Erreur lors de la validation du retour');
        this.saving.set(false);
      },
    });
  }

  trackBySortie(_: number, s: SortieARegulariserDto): number {
    return s.idSortie;
  }
}