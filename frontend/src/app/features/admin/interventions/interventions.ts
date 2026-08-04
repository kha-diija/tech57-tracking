import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { InterventionService } from '../../../shared/services/intervention.service';
import { Intervention, InterventionRequest } from '../../../shared/models/intervention.model';
// IMPORT DE PACKAGE AJOUTÉ ICI
import { LucideAngularModule, MapPin, CheckCircle, Clock, Calendar, Trash2, Plus, Download, Search, Edit, Eye, Activity, Map, Image as LucideImage, FileText, Package } from 'lucide-angular';

@Component({
  selector: 'app-interventions',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './interventions.html',
  styleUrl: './interventions.scss'
})
export class Interventions implements OnInit {
  // AJOUT DE Package DANS L'OBJET ICONS
  readonly icons = { 
    MapPin, CheckCircle, Clock, Calendar, Trash2, Plus, Download, Search, Edit, Eye, Activity, Map,
    Image: LucideImage, FileText, Package
  };
  
  private interventionService = inject(InterventionService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);
  
  interventions: Intervention[] = [];
  searchTerm: string = '';

  missionsList: any[] = [];
  techniciensList: any[] = [];

  showModal = false;
  isEditMode = false;
  selectedIntervention: Partial<InterventionRequest> & { id?: number } = {};

  showDetailModal = false;
  currentDetail: Intervention | null = null;

  showDeleteModal = false;
  deleteTargetId: number | null = null;

  kpis = {
    total: 0,
    enCours: 0,
    executees: 0,
    tauxMoyen: 0
  };

  ngOnInit(): void {
    this.loadInterventions();
    this.loadMissionsAndTechniciens();
  }

  loadInterventions() {
    this.interventionService.getInterventions().subscribe({
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
    if (!this.searchTerm.trim()) {
      return this.interventions;
    }
    const term = this.searchTerm.toLowerCase();
    return this.interventions.filter(item => 
      (item.missionReference && item.missionReference.toLowerCase().includes(term)) ||
      (item.technicienNom && item.technicienNom.toLowerCase().includes(term)) ||
      (item.statut && item.statut.toLowerCase().includes(term))
    );
  }

  calculerKPIs() {
    this.kpis.total = this.interventions.length;
    this.kpis.executees = this.interventions.filter(i => i.numeroVisite >= 2 || i.statut === 'Exécutée').length;
    this.kpis.enCours = this.interventions.filter(i => i.statut === 'En cours' || i.statut === 'Planifiée').length;
    
    const totalAvancement = this.interventions.reduce((sum, current) => sum + (current.tauxAvancement || 0), 0);
    this.kpis.tauxMoyen = this.interventions.length > 0 ? Math.round(totalAvancement / this.interventions.length) : 0;
  }

  exporterCSV() {
    if (this.interventions.length === 0) {
      alert("Aucune donnée à exporter.");
      return;
    }

    const headers = ['"ID"', '"Mission"', '"Technicien"', '"Date Debut"', '"Visites"', '"Avancement"', '"Statut"'];
    const rows = this.interventions.map(i => [
      `"${i.id}"`,
      `"${i.missionReference}"`,
      `"${i.technicienNom}"`,
      `"${i.dateDebut}"`,
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

  ouvrirModalCreation() {
    this.isEditMode = false;
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
      dateDebut: `${year}-${month}-${day}T${hours}:${minutes}`
    };
    this.showModal = true;
  }

  modifier(id: number) {
    const found = this.interventions.find(i => i.id === id);
    if (found) {
      this.isEditMode = true;
      this.selectedIntervention = { 
        ...found,
        dateDebut: found.dateDebut ? found.dateDebut.slice(0, 16) : ''
      };
      this.showModal = true;
    }
  }

  fermerModal() {
    this.showModal = false;
    this.selectedIntervention = {};
  }

  sauvegarderIntervention() {
    let formattedDate = this.selectedIntervention.dateDebut;
    if (formattedDate && !formattedDate.endsWith(':00') && formattedDate.length === 16) {
      formattedDate += ':00';
    }
    const payload: InterventionRequest = {
      dateDebut: formattedDate || new Date().toISOString().slice(0, 19),
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
        setTimeout(() => {
          this.loadInterventions();
          this.fermerModal();
        }, 400);
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

  downloadAttestation(id: number) {
    const url = `http://localhost:8080/api/admin/interventions/${id}/attestation/download`;
    this.http.get(url, {
      responseType: 'blob'
    }).subscribe({
      next: (blob: Blob) => {
        const fileUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = fileUrl;
        link.download = `attestation_intervention_${id}.txt`;
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