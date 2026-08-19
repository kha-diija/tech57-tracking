import { Component, OnInit, inject, ChangeDetectorRef, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as XLSX from 'xlsx';
import { AuthService } from '../../shared/services/auth.service';
import { TechnicienDashboardService } from '../../shared/services/technicien-dashboard.service';
import { TechnicienKpiResponse } from '../../shared/models/technicien-dashboard.model';
import { LucideAngularModule, Download, ArrowUpRight, CheckCircle2, Clock, MapPin, Building, AlertCircle, TrendingUp, PackageMinus, PackagePlus } from 'lucide-angular';

@Component({
  selector: 'app-technicien-dashboard',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  templateUrl: './technicien-dashboard.html',
  styleUrl: './technicien-dashboard.scss'
})
export class TechnicienDashboard implements OnInit {
  authService = inject(AuthService);
  private dashboardService = inject(TechnicienDashboardService);
  private cdr = inject(ChangeDetectorRef);

  kpis: TechnicienKpiResponse | null = null;
  loading = false;
  erreur: string | null = null;

  readonly icons = {
    Download, ArrowUpRight, CheckCircle2, Clock, MapPin, Building, AlertCircle, TrendingUp, PackageMinus, PackagePlus
  };

  prenomTechnicien = signal<string>('Technicien');
  activeDonutTooltip: { label: string; value: number; percent: number } | null = null;

  ngOnInit(): void {
    this.chargerInfosUtilisateur();
    this.chargerDonneesDashboard();
  }

  chargerInfosUtilisateur(): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      if (currentUser.prenom) {
        this.prenomTechnicien.set(currentUser.prenom);
      } else if (currentUser.nom) {
        this.prenomTechnicien.set(currentUser.nom);
      }
    }
  }

  chargerDonneesDashboard(): void {
    this.loading = true;
    this.erreur = null;

    this.dashboardService.getKpis().subscribe({
      next: (data: TechnicienKpiResponse) => {
        this.kpis = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        this.loading = false;
        this.erreur = "Impossible de charger les données du tableau de bord.";
        this.cdr.detectChanges();
        console.error(err);
      }
    });
  }

  statutClass(statut: string): string {
    switch (statut) {
      case 'Planifiée': return 'badge--warning';
      case 'En cours': return 'badge--info';
      case 'En retard': return 'badge--danger';
      case 'Exécutée': return 'badge--success';
      case 'Clôturée': return 'badge--gray';
      default: return '';
    }
  }

  // --- Répartition des interventions par statut, pour le donut ---
  get statutRepartition(): { label: string; value: number; color: string }[] {
    if (!this.kpis) return [];
    const planifiees = this.kpis.missionsActuelles.filter(m => m.statut === 'Planifiée').length;
    return [
      { label: 'Réalisées', value: this.kpis.interventionsRealisees, color: '#22c55e' },
      { label: 'En cours', value: this.kpis.interventionsEnCours, color: '#3b82f6' },
      { label: 'En retard', value: this.kpis.interventionsEnRetard, color: '#ef4444' },
      { label: 'Planifiées', value: planifiees, color: '#f59e0b' }
    ].filter(item => item.value > 0);
  }

  get statutTotal(): number {
    return this.statutRepartition.reduce((acc, item) => acc + item.value, 0);
  }

  statutPercent(value: number): number {
    const total = this.statutTotal;
    return total ? Math.round((value / total) * 100) : 0;
  }

  get donutGradient(): string {
    const items = this.statutRepartition;
    const total = this.statutTotal;
    if (!total || !items.length) return 'conic-gradient(#2a2f3a 0deg 360deg)';

    let cumulative = 0;
    const stops: string[] = [];
    for (const item of items) {
      const percent = (item.value / total) * 100;
      const start = cumulative;
      const end = cumulative + percent;
      stops.push(`${item.color} ${start}% ${end}%`);
      cumulative = end;
    }
    return `conic-gradient(${stops.join(', ')})`;
  }

  // --- Comparaison matériel sorti / rendu ---
  get materielMax(): number {
    if (!this.kpis) return 1;
    return Math.max(this.kpis.quantiteMaterielSortie, this.kpis.quantiteMaterielRendue, 1);
  }

  materielBarHeight(value: number): number {
    return Math.round((value / this.materielMax) * 100);
  }

  // --- Export Excel ---
  onExport(): void {
    if (!this.kpis) return;

    const wb = XLSX.utils.book_new();

    const kpiRows: (string | number)[][] = [
      ['Indicateur', 'Valeur'],
      ['Interventions réalisées', this.kpis.interventionsRealisees],
      ['En cours', this.kpis.interventionsEnCours],
      ['En retard', this.kpis.interventionsEnRetard],
      ['Avancement moyen (%)', this.kpis.tauxAvancementMoyen],
      ['Taux de conformité (%)', this.kpis.tauxConformite],
      ['Temps moyen / visite (min)', this.kpis.tempsMoyenInterventionMinutes],
      ['Anomalies détectées', this.kpis.anomaliesDetectees],
      ['Matériel sorti (unités)', this.kpis.quantiteMaterielSortie],
      ['Matériel rendu (unités)', this.kpis.quantiteMaterielRendue],
      ['Établissements assignés', this.kpis.etablissementsCount]
    ];
    const wsKpi = XLSX.utils.aoa_to_sheet(kpiRows);
    XLSX.utils.book_append_sheet(wb, wsKpi, 'KPI');

    const missionRows: (string | number)[][] = [
      ['ID', 'Titre', 'Établissement', 'Horaire prévu', 'Statut', 'Urgence']
    ];
    this.kpis.missionsActuelles.forEach(m => {
      missionRows.push([
        m.id,
        m.titre,
        m.etablissement,
        m.horaire ? new Date(m.horaire).toLocaleString('fr-FR') : '-',
        m.statut,
        m.urgence
      ]);
    });
    const wsMissions = XLSX.utils.aoa_to_sheet(missionRows);
    XLSX.utils.book_append_sheet(wb, wsMissions, 'Missions');

    const etabRows: (string | number)[][] = [['ID', 'Nom', 'Référence', 'Interventions']];
    this.kpis.etablissementsAssignes.forEach(e => {
      etabRows.push([e.id, e.nom, e.ville, e.interventions]);
    });
    const wsEtab = XLSX.utils.aoa_to_sheet(etabRows);
    XLSX.utils.book_append_sheet(wb, wsEtab, 'Établissements');

    const fileName = `dashboard-technicien-${new Date().toISOString().slice(0, 10)}.xlsx`;
    XLSX.writeFile(wb, fileName);
  }
}