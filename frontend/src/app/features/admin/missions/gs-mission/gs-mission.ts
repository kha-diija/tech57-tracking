import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  Briefcase,
  Calendar,
  CheckCircle2,
  Clock,
  Search,
  Plus,
  Download,
  Pencil,
  Trash2,
  X,
  Building2,
  User,
  Users,
  Eye,
  Package,
  AlertTriangle  // ✅ AJOUTÉ pour l'icône d'avertissement
} from 'lucide-angular';
import { MissionService } from '../../../../shared/services/mission.service';
import { EtablissementService } from '../../../../shared/services/etablissement.service';
import { EquipeTechniqueService } from '../../../../shared/services/equipe-technique.service';
import { MaterielService } from '../../../../shared/services/materiel.service';
import { AuthService } from '../../../../shared/services/auth.service';
import { MissionInstallation, MissionRequestDTO, MissionMateriel } from '../../../../shared/models/mission.model';
import { Etablissement } from '../../../../shared/models/etablissement.model';
import { Materiel } from '../../../../shared/models/materiel.model';

interface EquipeTechnique {
  idEquipe: number;
  nomEquipe: string;
}

interface MissionFormModel {
  reference: string;
  titre: string;
  statut: string;
  budgetPropose: number | null;
  idEtablissement: number | null;
  idAdministrateur: number | null;
  idEquipe: number | null;
}

@Component({
  selector: 'app-gs-mission',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gs-mission.html',
  styleUrl: './gs-mission.scss'
})
export class GsMission {
  private readonly missionService = inject(MissionService);
  private readonly etablissementService = inject(EtablissementService);
  private readonly equipeService = inject(EquipeTechniqueService);
  private readonly authService = inject(AuthService);
  private readonly materielService = inject(MaterielService);

  readonly icons = {
    Briefcase, Calendar, CheckCircle2, Clock, Search, Plus, Download,
    Pencil, Trash2, X, Building2, User, Users, Eye, Package, AlertTriangle  // ✅ AJOUTÉ
  };

  readonly statutOptions = ['PROPOSEE', 'Planifiée', 'En cours', 'Terminée', 'Annulée'];

  // Données réactives
  readonly missions = signal<MissionInstallation[]>([]);
  readonly etablissementsList = signal<Etablissement[]>([]);
  readonly equipesList = signal<EquipeTechnique[]>([]);
  readonly materielsList = signal<Materiel[]>([]);
  readonly isLoading = signal<boolean>(true);

  // Filtres & Recherche
  readonly searchTerm = signal<string>('');
  readonly selectedStatutFilter = signal<string>('tous');

  // Modale de confirmation
  readonly confirmModal = signal<{
    isOpen: boolean;
    title: string;
    message: string;
    isError?: boolean;
    item: MissionInstallation | null;
    force: boolean;
  }>({
    isOpen: false,
    title: '',
    message: '',
    isError: false,
    item: null,
    force: false
  });

  // Modale des matériels
  readonly showMaterielsModal = signal<MissionInstallation | null>(null);

  // ✅ NOUVEAU : Modale de rejet personnalisée
  readonly showRejetModal = signal<{
    isOpen: boolean;
    missionId: number | null;
    motif: string;
  }>({
    isOpen: false,
    missionId: null,
    motif: ''
  });

  // Filtrage dynamique
  readonly filteredMissions = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const statut = this.selectedStatutFilter();
    return this.missions().filter((m) => {
      const matchesStatut = statut === 'tous' || m.statut === statut;
      const matchesTerm =
        !term ||
        m.titre.toLowerCase().includes(term) ||
        m.reference.toLowerCase().includes(term) ||
        m.etablissementDesignation?.toLowerCase().includes(term);
      return matchesStatut && matchesTerm;
    });
  });

  // Panneau formulaire
  readonly showForm = signal<boolean>(false);
  readonly editingId = signal<number | null>(null);
  readonly formModel = signal<MissionFormModel>(this.emptyForm());

  // Responsable de l'établissement sélectionné
  readonly selectedResponsable = computed(() => {
    const idEtab = this.formModel().idEtablissement;
    if (!idEtab) return null;
    const etab = this.etablissementsList().find(e => e.idEtablissement === idEtab);
    return etab?.responsable ?? null;
  });

  // Nom complet de l'admin créateur d'origine
  private readonly editingAdminNomComplet = signal<string | null>(null);

  readonly creatorDisplay = computed(() => {
    const currentUser = this.authService.currentUser();
    if (this.editingId() === null) {
      return currentUser
        ? { id: currentUser.id, nomComplet: `${currentUser.prenom} ${currentUser.nom}` }
        : null;
    }
    const id = this.formModel().idAdministrateur;
    return id !== null
      ? { id, nomComplet: this.editingAdminNomComplet() ?? '—' }
      : null;
  });

  // Jointure côté client
  getResponsableForEtablissement(idEtablissement: number) {
    return this.etablissementsList().find(e => e.idEtablissement === idEtablissement)?.responsable ?? null;
  }

  getEquipeNameForMission(idEquipe: number | null | undefined): string {
    if (!idEquipe) return 'Aucune équipe';
    const equipe = this.equipesList().find(eq => eq.idEquipe === idEquipe);
    return equipe ? equipe.nomEquipe : 'Équipe inconnue';
  }

  getMaterielName(idMateriel: number): string {
    const materiel = this.materielsList().find(m => m.idMateriel === idMateriel);
    return materiel ? `${materiel.nom} (${materiel.reference})` : 'Matériel inconnu';
  }

  // Ouvrir la modale des matériels
  ouvrirMateriels(mission: MissionInstallation): void {
    this.showMaterielsModal.set(mission);
  }

  fermerMateriels(): void {
    this.showMaterielsModal.set(null);
  }

  // ✅ NOUVELLES MÉTHODES POUR LA MODALE DE REJET
  ouvrirRejetModal(missionId: number): void {
    this.showRejetModal.set({
      isOpen: true,
      missionId: missionId,
      motif: ''
    });
  }

  fermerRejetModal(): void {
    this.showRejetModal.set({
      isOpen: false,
      missionId: null,
      motif: ''
    });
  }

  updateMotifRejet(value: string): void {
    this.showRejetModal.update((m) => ({ ...m, motif: value }));
  }

  confirmerRejet(): void {
    const modal = this.showRejetModal();
    if (!modal.missionId || !modal.motif.trim()) {
      return;
    }
    
    this.missionService.rejeter(modal.missionId, modal.motif).subscribe({
      next: () => {
        this.fermerRejetModal();
        this.loadData();
      },
      error: (err) => {
        console.error('Erreur lors du rejet', err);
        this.fermerRejetModal();
      }
    });
  }

  private emptyForm(): MissionFormModel {
    return {
      reference: '',
      titre: '',
      statut: 'Planifiée',
      budgetPropose: null,
      idEtablissement: null,
      idAdministrateur: null,
      idEquipe: null
    };
  }

  constructor() {
    this.loadData();
  }

  private loadData(): void {
    this.isLoading.set(true);
    this.missionService.getAll().subscribe({
      next: (data) => {
        this.missions.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });

    this.etablissementService.getAll().subscribe((data) => {
      this.etablissementsList.set(data);
    });

    this.equipeService.getAll().subscribe((data: any) => {
      this.equipesList.set(data);
    });

    this.materielService.getAll().subscribe((data) => {
      this.materielsList.set(data);
    });
  }

  // ✅ Approuver la mission
  approuverMission(missionId: number): void {
    this.missionService.approuver(missionId).subscribe({
      next: () => {
        this.loadData();
      },
      error: (err) => {
        console.error('Erreur lors de l\'approbation', err);
      }
    });
  }

  // ✅ MODIFIÉ : Rejeter la mission avec modale personnalisée
  rejeterMission(missionId: number): void {
    this.ouvrirRejetModal(missionId);
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.editingAdminNomComplet.set(null);
    const currentUser = this.authService.currentUser();
    this.formModel.set({
      ...this.emptyForm(),
      idAdministrateur: currentUser?.id ?? null
    });
    this.showForm.set(true);
  }

  openEditForm(item: MissionInstallation): void {
    this.editingId.set(item.idMission);
    this.editingAdminNomComplet.set(item.adminNomComplet ?? null);
    this.showForm.set(true);
    
    const currentUser = this.authService.currentUser();
    
    this.formModel.set({
      reference: item.reference,
      titre: item.titre,
      statut: item.statut,
      budgetPropose: item.budgetPropose ?? null,
      idEtablissement: item.idEtablissement ?? null,
      idAdministrateur: currentUser?.id ?? item.idAdministrateur ?? null,
      idEquipe: item.idEquipe ?? null
    });
  }

  closeForm(): void {
    this.showForm.set(false);
  }

  updateField<K extends keyof MissionFormModel>(field: K, value: MissionFormModel[K]): void {
    this.formModel.update((m) => ({ ...m, [field]: value }));
  }

  saveForm(): void {
    const m = this.formModel();
    if (!m.titre.trim() || !m.reference.trim() || !m.idEtablissement || !m.idAdministrateur) return;

    const payload: MissionRequestDTO = {
      reference: m.reference,
      titre: m.titre,
      statut: m.statut,
      budgetPropose: m.budgetPropose,
      idEtablissement: m.idEtablissement,
      idAdministrateur: m.idAdministrateur,
      idEquipe: m.idEquipe
    };

    const id = this.editingId();
    const request = id
      ? this.missionService.update(id, payload)
      : this.missionService.create(payload);

    request.subscribe({
      next: () => {
        this.closeForm();
        this.loadData();
      }
    });
  }

  deleteMission(item: MissionInstallation, force: boolean = false): void {
    if (!force) {
      this.confirmModal.set({
        isOpen: true,
        title: "Confirmer la suppression",
        message: `Voulez-vous vraiment supprimer la mission "${item.titre}" (${item.reference}) ?`,
        isError: false,
        item: item,
        force: false
      });
      return;
    }

    this.missionService.delete(item.idMission, force).subscribe({
      next: () => {
        this.closeConfirmModal();
        this.loadData();
      },
      error: (err) => {
        if (err.status === 409 && err.error?.message) {
          this.confirmModal.set({
            isOpen: true,
            title: "Suppression impossible (Dépendances)",
            message: `${err.error.message}\n\nVoulez-vous vraiment tout supprimer ?`,
            isError: true,
            item: item,
            force: true
          });
        } else {
          this.confirmModal.set({
            isOpen: true,
            title: "Erreur",
            message: "Une erreur est survenue lors de la suppression.",
            isError: true,
            item: null,
            force: false
          });
        }
      }
    });
  }

  executeDelete(): void {
    const modal = this.confirmModal();
    if (!modal.item) {
      this.closeConfirmModal();
      return;
    }

    const item = modal.item;
    const force = modal.force;

    this.missionService.delete(item.idMission, force).subscribe({
      next: () => {
        this.closeConfirmModal();
        this.loadData();
      },
      error: (err) => {
        if (err.status === 409 && err.error?.message) {
          this.confirmModal.set({
            isOpen: true,
            title: "Suppression impossible (Dépendances)",
            message: `${err.error.message}\n\nVoulez-vous vraiment tout supprimer ?`,
            isError: true,
            item: item,
            force: true
          });
        } else {
          this.closeConfirmModal();
        }
      }
    });
  }

  closeConfirmModal(): void {
    this.confirmModal.set({ isOpen: false, title: '', message: '', isError: false, item: null, force: false });
  }

  onExport(): void {
    const rows: string[] = ['Référence;Titre;Statut;Budget;Établissement;Équipe Technique;Responsable Établissement;Administrateur;ID Administrateur;Date de création'];
    this.filteredMissions().forEach((m) => {
      const resp = this.getResponsableForEtablissement(m.idEtablissement);
      const respNom = resp ? `${resp.prenom} ${resp.nom}` : '';
      const equipeNom = this.getEquipeNameForMission(m.idEquipe);
      rows.push(
        `${m.reference};${m.titre};${m.statut};${m.budgetPropose ?? 0};${m.etablissementDesignation ?? ''};${equipeNom};${respNom};${m.adminNomComplet ?? ''};${m.idAdministrateur ?? ''};${m.dateCreation}`
      );
    });

    const csvContent = rows.join('\n');
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `missions-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }
}