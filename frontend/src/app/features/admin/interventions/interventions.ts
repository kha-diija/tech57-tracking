import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { InterventionService } from '../../../shared/services/intervention.service';
import { Intervention, InterventionRequest } from '../../../shared/models/intervention.model';
import { LucideAngularModule, MapPin, CheckCircle, Clock, Calendar, Trash2, Plus, Download, Search, Edit, Eye, Activity, Map, Image as LucideImage, FileText, Package, AlertCircle } from 'lucide-angular';

@Component({
  selector: 'app-interventions',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './interventions.html',
  styleUrl: './interventions.scss'
})
export class Interventions implements OnInit {
  readonly icons = {
    MapPin, CheckCircle, Clock, Calendar, Trash2, Plus, Download, Search, Edit, Eye, Activity, Map,
    Image: LucideImage, FileText, Package, AlertCircle
  };

  private interventionService = inject(InterventionService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  interventions: Intervention[] = [];
  searchTerm: string = '';

  // --- filtre par statut : "En retard" ajouté ---
  selectedStatutFilter: string = 'tous';
  readonly statutOptions = ['Planifiée', 'En cours', 'En retard', 'Exécutée', 'Clôturée'];

  // statuts sélectionnables manuellement dans le formulaire (jamais "En retard", c'est calculé)
  readonly statutOptionsEditables = ['Planifiée', 'En cours', 'Exécutée'];

  missionsList: any[] = [];
  techniciensList: any[] = [];

  showModal = false;
  isEditMode = false;
  selectedIntervention: Partial<InterventionRequest> & { id?: number } = {};
  formError: string | null = null;

  showDetailModal = false;
  currentDetail: Intervention | null = null;

  showDeleteModal = false;
  deleteTargetId: number | null = null;

  showConfirmCloseModal = false;
  confirmCloseId: number | null = null;

  kpis = {
    total: 0,
    enCours: 0,
    executees: 0,
    enRetard: 0,
    tauxMoyen: 0
  };

  ngOnInit(): void {
    this.loadInterventions();
    this.loadMissionsAndTechniciens();
  }

  loadInterventions() {
    this.interventionService.getInterventionsNoCache().subscribe({
      next: (data) => {
        this.interventions = data || [];
        this.calculerKPIs();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des interventions', err);
      }
    });
  }

  loadMissionsAndTechniciens() {
    this.http.get<any[]>('http://localhost:8080/api/admin/missions').subscribe({
      next: (data) => this.missionsList = data || [],
      error: () => {
        this.missionsList = [{ idMission: 1, reference: 'MIS-001' }, { idMission: 2, reference: 'MIS-004' }, { idMission: 6, reference: 'MSN-006' }];
      }
    });

    this.http.get<any>('http://localhost:8080/api/admin/interventions/form-data').subscribe({
      next: (data) => {
        this.techniciensList = data.techniciens || [];
        this.cdr.detectChanges();
      },
      error: () => {
        this.techniciensList = [
          { id: 5, nom: 'Amrani', prenom: 'Youssef' },
          { id: 6, nom: 'Kabbaj', prenom: 'Sara' },
          { id: 7, nom: 'Naciri', prenom: 'Anas' },
          { id: 8, nom: 'Ziani', prenom: 'Fatima' }
        ];
      }
    });
  }

  get filteredInterventions(): Intervention[] {
    let result = this.interventions;

    if (this.selectedStatutFilter !== 'tous') {
      result = result.filter(item => item.statut === this.selectedStatutFilter);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(item =>
        (item.missionReference && item.missionReference.toLowerCase().includes(term)) ||
        (item.technicienNom && item.technicienNom.toLowerCase().includes(term)) ||
        (item.statut && item.statut.toLowerCase().includes(term))
      );
    }

    return result;
  }

  calculerKPIs() {
    this.kpis.total = this.interventions.length;
    this.kpis.executees = this.interventions.filter(i => i.statut === 'Exécutée' || i.statut === 'Clôturée').length;
    this.kpis.enCours = this.interventions.filter(i => i.statut === 'En cours').length;
    this.kpis.enRetard = this.interventions.filter(i => i.statut === 'En retard').length;

    const totalAvancement = this.interventions.reduce((sum, current) => sum + (current.tauxAvancement || 0), 0);
    this.kpis.tauxMoyen = this.interventions.length > 0 ? Math.round(totalAvancement / this.interventions.length) : 0;
  }

  // --- Utilitaires basés sur checkInOuts (pas "visites") ---
  aUneVisiteEnCours(item: Intervention): boolean {
    return !!item.checkInOuts?.some(v => v.dateHeureCheckin && !v.dateHeureCheckout);
  }

  nombreVisitesTerminees(item: Intervention): number {
    return item.checkInOuts?.filter(v => v.dateHeureCheckin && v.dateHeureCheckout).length || 0;
  }

  exporterCSV() {
    if (this.interventions.length === 0) {
      alert("Aucune donnée à exporter.");
      return;
    }
    const headers = ['"ID"', '"Mission"', '"Technicien"', '"Date Prevue"', '"Visites"', '"Avancement"', '"Statut"'];
    const rows = this.interventions.map(i => [
      `"${i.id}"`,
      `"${i.missionReference}"`,
      `"${i.technicienNom}"`,
      `"${i.datePrevue}"`,
      `"${i.numeroVisite}"`,
      `"${i.tauxAvancement}%"`,
      `"${i.statut}"`
    ]);
    const separator = ';';
    let csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(separator), ...rows.map(e => e.join(separator))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `suivi_interventions_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  // --- Création : seule la date PRÉVUE est demandée, plus dateDebut/numeroVisite ---
  ouvrirModalCreation() {
    this.isEditMode = false;
    this.formError = null;
    const now = new Date();
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    this.selectedIntervention = {
      numeroVisite: 0,
      tauxAvancement: 0,
      statut: 'Planifiée',
      datePrevue: `${year}-${month}-${day}T${hours}:${minutes}`
    };
    this.showModal = true;
  }

  modifier(id: number) {
    const found = this.interventions.find(i => i.id === id);
    if (found) {
      this.isEditMode = true;
      this.formError = null;
      this.selectedIntervention = {
        ...found,
        // "En retard" n'est jamais une valeur éditable : on retombe sur "Planifiée"
        statut: found.statut === 'En retard' ? 'Planifiée' : found.statut,
        datePrevue: found.datePrevue ? found.datePrevue.slice(0, 16) : '',
        dateDebut: found.dateDebut ? found.dateDebut.slice(0, 16) : undefined,
        dateFin: found.dateFin ? found.dateFin.slice(0, 16) : undefined
      };
      this.showModal = true;
    }
  }

  fermerModal() {
    this.showModal = false;
    this.selectedIntervention = {};
    this.formError = null;
  }

  sauvegarderIntervention() {
    this.formError = null;

    let formattedDatePrevue = this.selectedIntervention.datePrevue;
    if (formattedDatePrevue && formattedDatePrevue.length === 16) {
      formattedDatePrevue += ':00';
    }

    const payload: InterventionRequest = {
      datePrevue: formattedDatePrevue || new Date().toISOString().slice(0, 19),
      dateDebut: this.selectedIntervention.dateDebut ? this.selectedIntervention.dateDebut + ':00' : undefined,
      dateFin: this.selectedIntervention.dateFin ? this.selectedIntervention.dateFin + ':00' : undefined,
      tauxAvancement: Number(this.selectedIntervention.tauxAvancement ?? 0),
      numeroVisite: Number(this.selectedIntervention.numeroVisite ?? 0),
      statut: this.selectedIntervention.statut || 'Planifiée',
      localisationGps: this.selectedIntervention.localisationGps || '33.8935,-5.5473',
      missionId: Number(this.selectedIntervention.missionId),
      technicienId: Number(this.selectedIntervention.technicienId)
    };

    const request$ = this.isEditMode && this.selectedIntervention.id
      ? this.interventionService.updateIntervention(this.selectedIntervention.id, payload)
      : this.interventionService.createIntervention(payload);

    request$.subscribe({
      next: () => {
        this.loadInterventions();
        this.fermerModal();
      },
      error: (err) => {
        console.error("Erreur technique backend :", err);
        this.formError = err?.error?.message || err?.error || "Erreur lors de l'enregistrement.";
      }
    });
  }

  getCityFromGps(gps?: string): string {
    if (!gps) return 'Non renseignée';
    return 'Meknès, Maroc (Approx.)';
  }

  onImageError(event: any) {
    event.target.src = 'assets/placeholder.png';
  }

  voirDetails(id: number) {
    this.interventionService.getInterventionById(id).subscribe({
      next: (data) => {
        this.currentDetail = data;
        this.showDetailModal = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error("Erreur lors du chargement des détails", err);
      }
    });
  }

  fermerDetailModal() {
    this.showDetailModal = false;
    this.currentDetail = null;
  }

  genererRapport(id: number) {
    const url = `http://localhost:8080/api/admin/interventions/${id}/rapport/download`;
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const fileUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = fileUrl;
        link.download = `rapport_intervention_${id}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(fileUrl);
      },
      error: (err) => {
        console.error("Erreur lors de la génération du rapport:", err);
        alert("Impossible de générer le rapport.");
      }
    });
  }

  ouvrirConfirmationCloture(id: number) {
    const intervention = this.interventions.find(i => i.id === id)
      || (this.currentDetail?.id === id ? this.currentDetail : null);

    if (intervention && this.aUneVisiteEnCours(intervention)) {
      alert("Impossible de clôturer : une visite est encore en cours (pas de check-out enregistré).");
      return;
    }
    if (intervention && this.nombreVisitesTerminees(intervention) < 2) {
      alert("Impossible de clôturer : au moins 2 visites terminées (check-in + check-out) sont requises.");
      return;
    }

    this.confirmCloseId = id;
    this.showConfirmCloseModal = true;
  }

  annulerCloture() {
    this.showConfirmCloseModal = false;
    this.confirmCloseId = null;
  }

  confirmerCloture() {
    if (this.confirmCloseId === null) return;

    this.interventionService.forceCompleteIntervention(this.confirmCloseId).subscribe({
      next: () => {
        this.fermerDetailModal();
        this.showConfirmCloseModal = false;
        this.confirmCloseId = null;
        setTimeout(() => {
          this.loadInterventions();
        }, 300);
      },
      error: (err) => {
        console.error("Erreur lors de la clôture:", err);
        const msg = err?.error?.message || err?.error || "Erreur lors de la clôture de l'intervention.";
        alert(msg);
      }
    });
  }

  downloadAttestation(id: number) {
    const url = `http://localhost:8080/api/admin/interventions/${id}/attestation/download`;
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: (blob: Blob) => {
        const fileUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = fileUrl;
        link.download = `attestation_intervention_${id}.pdf`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(fileUrl);
      },
      error: (err) => {
        console.error("Erreur lors du téléchargement de l'attestation:", err);
        alert("Impossible de télécharger l'attestation. Vérifiez votre connexion ou vos droits d'accès.");
      }
    });
  }

  ouvrirModalSuppression(id: number) {
    this.deleteTargetId = id;
    this.showDeleteModal = true;
  }

  confirmerSuppression() {
    if (this.deleteTargetId === null) return;
    this.interventionService.deleteIntervention(this.deleteTargetId).subscribe({
      next: () => {
        this.interventions = this.interventions.filter(item => item.id !== this.deleteTargetId);
        this.calculerKPIs();
        this.fermerModalSuppression();
        this.cdr.detectChanges();
      },
      error: () => {
        this.interventions = this.interventions.filter(item => item.id !== this.deleteTargetId);
        this.calculerKPIs();
        this.fermerModalSuppression();
        this.cdr.detectChanges();
      }
    });
  }

  fermerModalSuppression() {
    this.showDeleteModal = false;
    this.deleteTargetId = null;
  }
}