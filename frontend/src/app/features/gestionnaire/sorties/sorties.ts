import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  PackageCheck,
  Clock,
  User,
  MapPin,
  AlertTriangle,
  RefreshCw,
  X,
  CheckCircle2,
  XCircle,
} from 'lucide-angular';
import { SortieMaterielGestionService } from './services/sortie-materiel.service';
import { SortieMaterielDto } from './models/sortie.model';
import { ConfirmationDialogComponent } from '../../../shared/components/confirmation-dialog/confirmation-dialog.component';
import { ConfirmationService } from '../../../shared/services/confirmation.service';

type StatutFiltre = 'En attente' | 'Validée' | 'Rejetée';

@Component({
  selector: 'app-sorties-stock',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule, ConfirmationDialogComponent],
  templateUrl: './sorties.html',
  styleUrl: './sorties.scss',
})
export class SortiesStock implements OnInit {
  private sortieService = inject(SortieMaterielGestionService);
  private confirmationService = inject(ConfirmationService);

  readonly icons = { PackageCheck, Clock, User, MapPin, AlertTriangle, RefreshCw, X, CheckCircle2, XCircle };

  statuts: StatutFiltre[] = ['En attente', 'Validée', 'Rejetée'];
  statutFiltre = signal<StatutFiltre>('En attente');

  sorties = signal<SortieMaterielDto[]>([]);
  loading = signal(false);
  actionLoading = signal<number | null>(null);
  errorMessage = signal<string | null>(null);

  showRejetModal = signal(false);
  sortieARejeter: SortieMaterielDto | null = null;
  motifRejet = '';

  ngOnInit(): void {
    this.charger();
  }

  setStatutFiltre(s: StatutFiltre): void {
    this.statutFiltre.set(s);
    this.charger();
  }

  charger(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.sortieService.lister(this.statutFiltre()).subscribe({
      next: (res) => { this.sorties.set(res); this.loading.set(false); },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Erreur lors du chargement des demandes');
        this.loading.set(false);
      },
    });
  }

  /**
   * Approuver une sortie avec confirmation modal (orange)
   */
  async approuver(s: SortieMaterielDto): Promise<void> {
    const confirmed = await this.confirmationService.confirm({
      title: 'Approuver la sortie',
      message: `Approuver la sortie #${s.idSortie} pour ${s.technicienNom} ? Le matériel sera considéré comme distribué.`,
      confirmText: 'Approuver',
      cancelText: 'Annuler',
      variant: 'warning' // Orange
    });

    if (!confirmed) return;

    this.actionLoading.set(s.idSortie);
    this.errorMessage.set(null);
    this.sortieService.approuver(s.idSortie).subscribe({
      next: () => { 
        this.actionLoading.set(null); 
        this.charger(); 
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? "Erreur lors de l'approbation");
        this.actionLoading.set(null);
      },
    });
  }

  ouvrirRejet(s: SortieMaterielDto): void {
    this.sortieARejeter = s;
    this.motifRejet = '';
    this.errorMessage.set(null);
    this.showRejetModal.set(true);
  }

  fermerRejet(): void {
    this.showRejetModal.set(false);
    this.sortieARejeter = null;
  }

  confirmerRejet(): void {
    if (!this.sortieARejeter) return;
    if (!this.motifRejet.trim()) {
      this.errorMessage.set('Le motif de rejet est obligatoire');
      return;
    }
    const idSortie = this.sortieARejeter.idSortie;
    this.actionLoading.set(idSortie);
    this.sortieService.rejeter(idSortie, { motifRejet: this.motifRejet.trim() }).subscribe({
      next: () => {
        this.actionLoading.set(null);
        this.fermerRejet();
        this.charger();
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Erreur lors du rejet');
        this.actionLoading.set(null);
      },
    });
  }

  classeStatut(statut: string): string {
    switch (statut) {
      case 'En attente': return 'statut-attente';
      case 'Validée': return 'statut-validee';
      case 'Rejetée': return 'statut-rejetee';
      default: return '';
    }
  }

  stockInsuffisant(d: { quantiteDemandee: number; stockDisponible: number | null }): boolean {
    return d.stockDisponible !== null && d.quantiteDemandee > d.stockDisponible;
  }

  trackBySortie(_: number, s: SortieMaterielDto): number {
    return s.idSortie;
  }
}
