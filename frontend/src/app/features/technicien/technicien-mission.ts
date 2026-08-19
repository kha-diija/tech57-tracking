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
  X,
  Building2,
  User,
  Users
} from 'lucide-angular';
import { TechnicienMissionService } from '../../shared/services/technicien-mission.service';
import { EtablissementService } from '../../shared/services/etablissement.service';
import { AuthService } from '../../shared/services/auth.service';
import { MissionInstallation, MissionRequestDTO } from '../../shared/models/mission.model';
import { Etablissement } from '../../shared/models/etablissement.model';

interface MissionFormModel {
  reference: string;
  titre: string;
  statut: string;
  budgetPropose: number | null;
  idEtablissement: number | null;
  idAdministrateur: number | null;
}

@Component({
  selector: 'app-technicien-mission',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './technicien-mission.html',
  styleUrls: ['./technicien-mission.scss']
})
export class TechnicienMissionComponent {
  private readonly techMissionService = inject(TechnicienMissionService);
  private readonly etablissementService = inject(EtablissementService);
  private readonly authService = inject(AuthService);

  readonly icons = {
    Briefcase, Calendar, CheckCircle2, Clock, Search, Plus, Download,
    Pencil, X, Building2, User, Users
  };

  readonly statutOptions = ['Planifiée', 'En cours', 'Terminée', 'Annulée'];

  // Données réactives
  readonly missions = signal<MissionInstallation[]>([]);
  readonly etablissementsList = signal<Etablissement[]>([]);
  readonly isLoading = signal<boolean>(true);

  // Filtres & Recherche
  readonly searchTerm = signal<string>('');
  readonly selectedStatutFilter = signal<string>('tous');

  // Filtrage dynamique (inclut maintenant le filtrage par nom d'équipe)
  readonly filteredMissions = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const statut = this.selectedStatutFilter();

    return this.missions().filter((m) => {
      const matchesStatut = statut === 'tous' || m.statut === statut;
      const matchesTerm =
        !term ||
        m.titre.toLowerCase().includes(term) ||
        m.reference.toLowerCase().includes(term) ||
        m.etablissementDesignation?.toLowerCase().includes(term) ||
        (m.equipeNom && m.equipeNom.toLowerCase().includes(term));
      return matchesStatut && matchesTerm;
    });
  });

  // Panneau formulaire
  readonly showForm = signal<boolean>(false);
  readonly editingId = signal<number | null>(null);
  readonly formModel = signal<MissionFormModel>(this.emptyForm());

  readonly selectedResponsable = computed(() => {
    const idEtab = this.formModel().idEtablissement;
    if (!idEtab) return null;
    const etab = this.etablissementsList().find(e => e.idEtablissement === idEtab);
    return etab?.responsable ?? null;
  });

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
      idAdministrateur: null
    };
  }

  constructor() {
    this.loadData();
  }

  private loadData(): void {
    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    
    // Charge uniquement les missions de l'équipe du technicien connecté
    this.techMissionService.getByTechnicien(currentUser.id).subscribe({
      next: (data) => {
        this.missions.set(data);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });

    this.etablissementService.getAll().subscribe((data) => {
      this.etablissementsList.set(data);
    });
  }

  openCreateForm(): void {
    this.editingId.set(null);
    const currentUser = this.authService.currentUser();
    this.formModel.set({
      ...this.emptyForm(),
      idAdministrateur: currentUser?.id ?? null
    });
    this.showForm.set(true);
  }

  openEditForm(item: MissionInstallation): void {
    this.editingId.set(item.idMission);
    this.showForm.set(true);
    this.formModel.set({
      reference: item.reference,
      titre: item.titre,
      statut: item.statut,
      budgetPropose: item.budgetPropose ?? null,
      idEtablissement: item.idEtablissement ?? null,
      idAdministrateur: item.idAdministrateur ?? null
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
    const currentUser = this.authService.currentUser();
    if (!m.titre.trim() || !m.reference.trim() || !m.idEtablissement || !currentUser) return;

    const payload: MissionRequestDTO = {
      reference: m.reference,
      titre: m.titre,
      statut: m.statut,
      budgetPropose: m.budgetPropose,
      idEtablissement: m.idEtablissement,
      idAdministrateur: m.idAdministrateur ?? currentUser.id,
      idEquipe: undefined // Géré automatiquement par le backend sur l'équipe du technicien
    };

    const id = this.editingId();
    const request = id
      ? this.techMissionService.update(id, payload)
      : this.techMissionService.create(payload, currentUser.id);

    request.subscribe({
      next: () => {
        this.closeForm();
        this.loadData();
      }
    });
  }

  onExport(): void {
    const rows: string[] = ['Référence;Titre;Statut;Budget;Établissement;Équipe;Responsable Établissement;Date de création'];
    this.filteredMissions().forEach((m) => {
      const resp = this.getResponsableForEtablissement(m.idEtablissement);
      const respNom = resp ? `${resp.prenom} ${resp.nom}` : '';
      rows.push(
        `${m.reference};${m.titre};${m.statut};${m.budgetPropose ?? 0};${m.etablissementDesignation ?? ''};${m.equipeNom ?? ''};${respNom};${m.dateCreation}`
      );
    });

    const csvContent = rows.join('\n');
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `mes-missions-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }
}