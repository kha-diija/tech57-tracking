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
  Users,
  Eye
} from 'lucide-angular';
import { TechnicienMissionService } from '../../shared/services/technicien-mission.service';
import { EtablissementService } from '../../shared/services/etablissement.service';
import { CommuneService } from '../../shared/services/commune.service';
import { MaterielService } from '../../shared/services/materiel.service';
import { AuthService } from '../../shared/services/auth.service';
import { MissionInstallation, MissionRequestDTO, MissionMateriel } from '../../shared/models/mission.model';
import { Etablissement, Commune } from '../../shared/models/etablissement.model';
import { Materiel } from '../../shared/models/materiel.model';

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
  private readonly communeService = inject(CommuneService);
  private readonly materielService = inject(MaterielService);
  private readonly authService = inject(AuthService);

  readonly icons = {
    Briefcase, Calendar, CheckCircle2, Clock, Search, Plus, Download,
    Pencil, X, Building2, User, Users, Eye
  };

  readonly statutOptions = ['PROPOSEE', 'Planifiée', 'En cours', 'Terminée', 'Annulée'];

  // Données réactives
  readonly missions = signal<MissionInstallation[]>([]);
  readonly etablissementsList = signal<Etablissement[]>([]);
  readonly communesList = signal<Commune[]>([]);
  readonly etablissementsFiltres = signal<Etablissement[]>([]);
  readonly selectedCommuneId = signal<number | null>(null);
  readonly materielsList = signal<Materiel[]>([]);
  readonly isLoading = signal<boolean>(true);

  // Filtres & Recherche
  readonly searchTerm = signal<string>('');
  readonly selectedStatutFilter = signal<string>('tous');

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
        m.etablissementDesignation?.toLowerCase().includes(term) ||
        (m.equipeNom && m.equipeNom.toLowerCase().includes(term));
      return matchesStatut && matchesTerm;
    });
  });

  // Panneau formulaire
  readonly showForm = signal<boolean>(false);
  readonly editingId = signal<number | null>(null);
  readonly formModel = signal<MissionFormModel>(this.emptyForm());

  // Matériels sélectionnés avec quantités
  readonly selectedMateriels = signal<MissionMateriel[]>([]);

  // Modale des matériels
  readonly showMaterielsModal = signal<MissionInstallation | null>(null);

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
    this.loadMateriels();
  }

  private loadData(): void {
    const currentUser = this.authService.currentUser();
    if (!currentUser) {
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    
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

    // Charger les communes
    this.communeService.getAll().subscribe((data) => {
      this.communesList.set(data);
    });
  }

  // Charger la liste des matériels
  private loadMateriels(): void {
    this.materielService.getAll().subscribe((data) => {
      this.materielsList.set(data);
    });
  }

  // ✅ Quand la commune change, filtrer les établissements
  onCommuneChange(idCommune: number | null): void {
    this.selectedCommuneId.set(idCommune);
    if (idCommune) {
      this.etablissementService.getByCommune(idCommune).subscribe((data) => {
        this.etablissementsFiltres.set(data);
        // Réinitialiser l'établissement sélectionné
        this.updateField('idEtablissement', null);
      });
    } else {
      this.etablissementsFiltres.set([]);
    }
  }

  // Ouvrir la modale des matériels
  ouvrirMateriels(item: MissionInstallation): void {
    this.showMaterielsModal.set(item);
  }

  fermerMateriels(): void {
    this.showMaterielsModal.set(null);
  }

  // Ajouter un matériel à la sélection
  addMateriel(idMateriel: number) {
    if (!idMateriel) return;
    
    const exists = this.selectedMateriels().some(m => m.idMateriel === idMateriel);
    if (exists) return;

    this.selectedMateriels.update(list => [...list, { idMateriel, quantite: 1 }]);
  }

  // Ajouter un matériel à partir d'un événement
  addMaterielFromEvent(event: any) {
    const value = event.target.value;
    if (value) {
      this.addMateriel(+value);
    }
  }

  // Mettre à jour la quantité à partir d'un événement
  updateQuantiteFromEvent(index: number, event: any) {
    const value = event.target.value;
    if (value) {
      this.updateQuantite(index, +value);
    }
  }

  // Retirer un matériel de la sélection
  removeMateriel(index: number) {
    this.selectedMateriels.update(list => list.filter((_, i) => i !== index));
  }

  // Changer la quantité d'un matériel
  updateQuantite(index: number, quantite: number) {
    if (quantite < 1) return;
    this.selectedMateriels.update(list => list.map((m, i) => i === index ? { ...m, quantite } : m));
  }

  // Récupérer le nom d'un matériel par son ID
  getMaterielName(idMateriel: number): string {
    const materiel = this.materielsList().find(m => m.idMateriel === idMateriel);
    return materiel ? `${materiel.nom} (${materiel.reference})` : 'Matériel inconnu';
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.selectedMateriels.set([]);
    this.selectedCommuneId.set(null);
    this.etablissementsFiltres.set([]);
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

    this.selectedMateriels.set(item.materiels || []);

    this.formModel.set({
      reference: item.reference,
      titre: item.titre,
      statut: item.statut,
      budgetPropose: item.budgetPropose ?? null,
      idEtablissement: item.idEtablissement ?? null,
      idAdministrateur: item.idAdministrateur ?? null
    });

    // Si l'établissement est déjà sélectionné, charger la commune correspondante
    if (item.idEtablissement) {
      const etab = this.etablissementsList().find(e => e.idEtablissement === item.idEtablissement);
      if (etab) {
        this.selectedCommuneId.set(etab.idCommune);
        this.etablissementService.getByCommune(etab.idCommune).subscribe((data) => {
          this.etablissementsFiltres.set(data);
        });
      }
    }
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
      idEquipe: undefined,
      materiels: this.selectedMateriels()
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