import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Intervention } from '../../shared/models/intervention.model';
import { TechnicienInterventionForm } from '../../shared/models/intervention.model';
import { LucideAngularModule, MapPin, CheckCircle, Clock, Calendar, Trash2, Plus, Download, Search, Edit, Eye, Activity, Map, Image as LucideImage, FileText, Package, AlertCircle } from 'lucide-angular';
import { TechnicienInterventionService } from '../../shared/services/technicien-intervention.service';

@Component({
  selector: 'app-technicien-interventions',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './interventions.html',
  styleUrl: './interventions.scss'
})
export class TechnicienInterventions implements OnInit {
  readonly icons = {
    MapPin, CheckCircle, Clock, Calendar, Trash2, Plus, Download, Search, Edit, Eye, Activity, Map,
    Image: LucideImage, FileText, Package, AlertCircle
  };

  private technicienService = inject(TechnicienInterventionService);
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  interventions: Intervention[] = [];
  searchTerm: string = '';

  selectedStatutFilter: string = 'tous';
  readonly statutOptions = ['Planifiée', 'En cours', 'En retard', 'Exécutée', 'Clôturée'];
  readonly statutOptionsEditables = ['Planifiée', 'En cours', 'Exécutée'];

  missionsList: any[] = [];
  techniciensList: any[] = [];

  showModal = false;
  isEditMode = false;
  selectedIntervention: TechnicienInterventionForm = {
    missionId: null,
    datePrevue: '',
    numeroVisite: 0,
    tauxAvancement: 0,
    statut: 'Planifiée'
  };
  formError: string | null = null;

  showDetailModal = false;
  currentDetail: Intervention | null = null;

  showCheckoutModal = false;
  checkoutInterventionId: number | null = null;

  // --- NOUVEAUTÉS POUR LE CHECK-OUT (Tableaux pour le MULTIPLE) ---
  materielsList: any[] = []; 
  checkoutData: any = {
    materielSortiIds: [],     // <-- Tableau d'IDs pour la sélection multiple
    materielRetourIds: [],    // <-- Tableau d'IDs pour la sélection multiple
    etatMateriel: '',
    signataire: ''
  };
  selectedFiles: { file: File, type: string }[] = [];
  attestationFile: File | null = null;

  // --- DYNAMISATION DE LA CHECKLIST ---
  checkoutChecklist: { idMateriel: number, nom: string, conforme: boolean }[] = [];

  kpis = {
    total: 0,
    enCours: 0,
    executees: 0,
    enRetard: 0,
    tauxMoyen: 0
  };

  // --- TOAST (remplace les alert()) ---
  toast: { message: string, type: 'success' | 'error' } | null = null;
  private toastTimeout: any = null;

  ngOnInit(): void {
    this.loadInterventions();
    this.loadMissionsAndTechniciens();
    this.loadMateriels();
  }

  showToast(message: string, type: 'success' | 'error' = 'success') {
    if (this.toastTimeout) {
      clearTimeout(this.toastTimeout);
    }
    this.toast = { message, type };
    this.cdr.detectChanges();
    this.toastTimeout = setTimeout(() => {
      this.toast = null;
      this.cdr.detectChanges();
    }, 3000);
  }

  loadInterventions() {
    this.technicienService.getMesInterventions().subscribe({
      next: (data: Intervention[]) => {
        this.interventions = data || [];
        this.calculerKPIs();
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error('Erreur lors du chargement des interventions', err);
      }
    });
  }

  loadMissionsAndTechniciens() {
    this.http.get<any[]>('http://localhost:8080/api/technicien/interventions/missions').subscribe({
      next: (data: any[]) => {
        this.missionsList = data || [];
      },
      error: () => {
        this.missionsList = [{ idMission: 1, reference: 'MIS-001' }];
      }
    });
  }

  loadMateriels() {
    this.http.get<any[]>('http://localhost:8080/api/technicien/interventions/materiels').subscribe({
      next: (data) => {
        this.materielsList = data || [];
        this.checkoutChecklist = this.materielsList.map(m => ({
          idMateriel: m.idMateriel,
          nom: m.nom + ' (' + m.reference + ')',
          conforme: false
        }));
      },
      error: () => {
        this.materielsList = [
          { idMateriel: 1, nom: 'PC Portable', reference: 'PC-002' },
          { idMateriel: 2, nom: 'Datashow', reference: 'DS-002' }
        ];
        this.checkoutChecklist = this.materielsList.map(m => ({
          idMateriel: m.idMateriel,
          nom: m.nom + ' (' + m.reference + ')',
          conforme: false
        }));
      }
    });
  }

  getFileCount(type: 'Avant' | 'Après'): number {
    return this.selectedFiles.filter(f => f.type === type).length;
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
        (item.etablissementDesignation && item.etablissementDesignation.toLowerCase().includes(term))
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

  aUneVisiteEnCours(item: Intervention): boolean {
    return !!item.checkInOuts?.some(v => v.dateHeureCheckin && !v.dateHeureCheckout);
  }

  checkIn(id: number) {
    const gpsCheckin = "33.8935,-5.5473";
    this.technicienService.checkIn(id, gpsCheckin).subscribe({
      next: () => this.loadInterventions(),
      error: (err: any) => this.showToast("Erreur lors du check-in.", "error")
    });
  }

  ouvrirModalCheckout(id: number) {
    this.checkoutInterventionId = id;
    this.checkoutData = { 
      materielSortiIds: [], 
      materielRetourIds: [], 
      etatMateriel: '', 
      signataire: '' 
    };
    this.selectedFiles = [];
    this.attestationFile = null;
    if (this.checkoutChecklist.length > 0) {
      this.checkoutChecklist = this.checkoutChecklist.map(item => ({ ...item, conforme: false }));
    }
    this.showCheckoutModal = true;
  }

  fermerCheckout() {
    this.showCheckoutModal = false;
    this.checkoutInterventionId = null;
  }

  onFileSelected(event: any, type: 'Avant' | 'Après') {
    const files = event.target.files;
    if (files) {
      for (let file of files) {
        this.selectedFiles.push({ file, type });
      }
    }
  }

  onAttestationSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.attestationFile = file;
    }
  }

  confirmerCheckout() {
    if (this.checkoutInterventionId === null) return;

    const payload = {
      gpsCheckout: "33.8935,-5.5473",
      signataire: this.checkoutData.signataire,
      materielSortiIds: this.checkoutData.materielSortiIds || [],
      materielRetourIds: this.checkoutData.materielRetourIds || [],
      etatsRetours: this.checkoutData.etatMateriel ? [this.checkoutData.etatMateriel] : [],
      checklist: this.checkoutChecklist
    };

    this.technicienService.checkOut(this.checkoutInterventionId, payload, this.selectedFiles, this.attestationFile).subscribe({
      next: () => {
        this.fermerCheckout();
        this.loadInterventions();
        this.showToast("Enregistré avec succès !", "success");
      },
      error: (err: any) => {
        console.error("Erreur lors du check-out", err);
        this.showToast("Erreur lors de l'enregistrement du check-out.", "error");
      }
    });
  }

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
      missionId: null,
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
        id: found.id,
        missionId: found.missionId,
        datePrevue: found.datePrevue ? found.datePrevue.slice(0, 16) : '',
        dateDebut: found.dateDebut ? found.dateDebut.slice(0, 16) : undefined,
        dateFin: found.dateFin ? found.dateFin.slice(0, 16) : undefined,
        numeroVisite: found.numeroVisite,
        tauxAvancement: found.tauxAvancement,
        statut: found.statut === 'En retard' ? 'Planifiée' : found.statut
      };
      this.showModal = true;
    }
  }

  fermerModal() {
    this.showModal = false;
    this.selectedIntervention = {
      missionId: null,
      datePrevue: '',
      numeroVisite: 0,
      tauxAvancement: 0,
      statut: 'Planifiée'
    };
    this.formError = null;
  }

  sauvegarderIntervention() {
    this.formError = null;
    let formattedDatePrevue = this.selectedIntervention.datePrevue;
    if (formattedDatePrevue && formattedDatePrevue.length === 16) {
      formattedDatePrevue += ':00';
    }
    const payload = {
      datePrevue: formattedDatePrevue || new Date().toISOString().slice(0, 19),
      dateDebut: this.selectedIntervention.dateDebut ? this.selectedIntervention.dateDebut + ':00' : undefined,
      dateFin: this.selectedIntervention.dateFin ? this.selectedIntervention.dateFin + ':00' : undefined,
      tauxAvancement: Number(this.selectedIntervention.tauxAvancement ?? 0),
      numeroVisite: Number(this.selectedIntervention.numeroVisite ?? 0),
      statut: this.selectedIntervention.statut || 'Planifiée',
      missionId: Number(this.selectedIntervention.missionId ?? 0)
    };
    if (!this.selectedIntervention.id) {
      this.technicienService.createIntervention(payload).subscribe({
        next: () => {
          this.loadInterventions();
          this.fermerModal();
          this.showToast("Intervention créée avec succès !", "success");
        },
        error: (err: any) => {
          console.error("Erreur technique backend :", err);
          this.formError = err?.error?.message || err?.error || "Erreur lors de l'enregistrement.";
        }
      });
    } else {
      this.technicienService.updateIntervention(this.selectedIntervention.id, payload).subscribe({
        next: () => {
          this.loadInterventions();
          this.fermerModal();
          this.showToast("Intervention modifiée avec succès !", "success");
        },
        error: (err: any) => {
          console.error("Erreur technique backend :", err);
          this.formError = err?.error?.message || err?.error || "Erreur lors de l'enregistrement.";
        }
      });
    }
  }

    getCityFromGps(gps?: string): string {
    if (!gps) return 'Non renseignée';
    return 'Meknès, Maroc (Approx.)';
  }

  getPhotoUrl(chemin: string): string {
    // Si le chemin est vide ou null → on renvoie une chaîne vide (pas d'image)
    if (!chemin) return '';
    
    // Si c'est déjà une URL complète (http/https) → on la retourne telle quelle
    if (chemin.startsWith('http')) return chemin;
    
    // Sinon, on préfixe avec l'URL du backend
    return `http://localhost:8080${chemin}`;
  }

  onImageError(event: any) {
    // Cache simplement l'image si elle ne charge pas
    event.target.style.display = 'none';
  }
  

  voirDetails(id: number) {
    if (!id || id === 0) {
      this.showToast("ID d'intervention invalide.", "error");
      return;
    }
    this.technicienService.getInterventionById(id).subscribe({
      next: (data) => {
        this.currentDetail = data;
        this.showDetailModal = true;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.error("Erreur lors du chargement des détails", err);
        this.showToast("Impossible de charger les détails de cette intervention.", "error");
      }
    });
  }

  fermerDetailModal() {
    this.showDetailModal = false;
    this.currentDetail = null;
  }

  genererRapport(id: number) {
    this.technicienService.genererRapport(id).subscribe({
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
      error: (err: any) => {
        console.error("Erreur lors de la génération du rapport:", err);
        this.showToast("Impossible de générer le rapport.", "error");
      }
    });
  }

  downloadAttestation(id: number) {
    this.technicienService.downloadAttestation(id).subscribe({
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
      error: (err: any) => {
        console.error("Erreur lors du téléchargement de l'attestation:", err);
        this.showToast("Impossible de télécharger l'attestation.", "error");
      }
    });
  }

  toggleMateriel(list: number[], id: number) {
    const index = list.indexOf(id);
    if (index === -1) {
      list.push(id);
    } else {
      list.splice(index, 1);
    }
  }

  selectEtatMateriel(etat: string) {
    this.checkoutData.etatMateriel = etat;
  }

  exporterCSV() {
    if (this.interventions.length === 0) {
      this.showToast("Aucune donnée à exporter.", "error");
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
}