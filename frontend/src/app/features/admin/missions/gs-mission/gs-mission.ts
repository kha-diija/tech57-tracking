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
  Users
} from 'lucide-angular';
import { MissionService } from '../../../../shared/services/mission.service';
import { EtablissementService } from '../../../../shared/services/etablissement.service';
import { EquipeTechniqueService } from '../../../../shared/services/equipe-technique.service';
import { AuthService } from '../../../../shared/services/auth.service';
import { MissionInstallation, MissionRequestDTO } from '../../../../shared/models/mission.model';
import { Etablissement } from '../../../../shared/models/etablissement.model';

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

  readonly icons = {
    Briefcase, Calendar, CheckCircle2, Clock, Search, Plus, Download,
    Pencil, Trash2, X, Building2, User, Users
  };

  readonly statutOptions = ['Planifiée', 'En cours', 'Terminée', 'Annulée'];

  // Données réactives
  readonly missions = signal<MissionInstallation[]>([]);
  readonly etablissementsList = signal<Etablissement[]>([]);
  readonly equipesList = signal<EquipeTechnique[]>([]);
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

  // Responsable de l'établissement sélectionné (affichage informatif seulement,
  // n'est jamais envoyé au backend : MissionInstallation n'a pas ce champ)
  readonly selectedResponsable = computed(() => {
    const idEtab = this.formModel().idEtablissement;
    if (!idEtab) return null;
    const etab = this.etablissementsList().find(e => e.idEtablissement === idEtab);
    return etab?.responsable ?? null;
  });

  // Nom complet de l'admin créateur d'origine (utile uniquement en mode édition,
  // car en création l'admin créateur est toujours l'admin connecté)
  private readonly editingAdminNomComplet = signal<string | null>(null);

  // Affichage de l'admin "créateur" de la mission dans le formulaire :
  // - en création : l'admin actuellement connecté (celui qui va créer la mission)
  // - en édition : l'admin d'origine ayant créé la mission (non modifiable)
  readonly creatorDisplay = computed(() => {
    const currentUser = this.authService.currentUser();
    if (this.editingId() === null) {
      // Mode création : c'est l'admin connecté
      return currentUser
        ? { id: currentUser.id, nomComplet: `${currentUser.prenom} ${currentUser.nom}` }
        : null;
    }
    // Mode édition : l'admin d'origine de la mission
    const id = this.formModel().idAdministrateur;
    return id !== null
      ? { id, nomComplet: this.editingAdminNomComplet() ?? '—' }
      : null;
  });

  // Jointure côté client : le DTO de mission ne contient pas le responsable de
  // l'établissement, mais on l'a déjà en mémoire via etablissementsList().
  // Utilisé dans le tableau pour afficher le responsable de chaque établissement.
  getResponsableForEtablissement(idEtablissement: number) {
    return this.etablissementsList().find(e => e.idEtablissement === idEtablissement)?.responsable ?? null;
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
    this.formModel.set({
      reference: item.reference,
      titre: item.titre,
      statut: item.statut,
      budgetPropose: item.budgetPropose ?? null,
      idEtablissement: item.idEtablissement ?? null,
      idAdministrateur: item.idAdministrateur ?? null,
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
    const rows: string[] = ['Référence;Titre;Statut;Budget;Établissement;Responsable Établissement;Administrateur;ID Administrateur;Date de création'];
    this.filteredMissions().forEach((m) => {
      const resp = this.getResponsableForEtablissement(m.idEtablissement);
      const respNom = resp ? `${resp.prenom} ${resp.nom}` : '';
      rows.push(
        `${m.reference};${m.titre};${m.statut};${m.budgetPropose ?? 0};${m.etablissementDesignation ?? ''};${respNom};${m.adminNomComplet ?? ''};${m.idAdministrateur ?? ''};${m.dateCreation}`
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