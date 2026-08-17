import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../shared/services/auth.service';
import { AchatMaterielService } from './services/achat-materiel.service';
import { EtablissementService } from '../../shared/services/etablissement.service';
import { Etablissement } from '../../shared/models/etablissement.model';
import { CreerAchatRequest } from './models/achat.model';
import { ShoppingCart } from 'lucide-angular';
import {
  LucideAngularModule,
  Search,
  SlidersHorizontal,
  Plus,
  QrCode,
  Laptop,
  Projector,
  Cable,
  Router,
  Boxes,
  Package,
  Wrench,
  History,
  Trash2,
  Pencil,
  X,
  ChevronRight,
  ChevronDown,
  AlertTriangle,
  CheckCircle2,
  PackagePlus,
  RefreshCw,
  Building2,
  Info,
} from 'lucide-angular';
import { MaterielService, RechercheMaterielParams } from './services/materiel.service';
import {
  CategorieMateriel,
  ComposantRequest,
  ETATS,
  EtatMateriel,
  KIT_COMPOSANTS_DEFAUT,
  KitRequest,
  MaintenanceDTO,
  MaintenanceRequest,
  MaterielDTO,
  MaterielRequest,
  MouvementMateriel,
} from './models/materiel.model';

type OngletDetail = 'info' | 'mouvements' | 'maintenance';
type ModeCreation = 'simple' | 'kit';

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './stock.html',
  styleUrl: './stock.scss',
})
export class Stock implements OnInit {
  private materielService = inject(MaterielService);
  private authService = inject(AuthService);
  private achatMaterielService = inject(AchatMaterielService);
  private etablissementService = inject(EtablissementService);

  readonly icons = {
    Search, SlidersHorizontal, Plus, QrCode, Laptop, Projector, Cable, Router,
    Boxes, Package, Wrench, History, Trash2, Pencil, X, ChevronRight, ChevronDown,
    AlertTriangle, CheckCircle2, PackagePlus, RefreshCw, Building2, Info,
    ShoppingCart,
  };

  readonly etats = ETATS;

  // --- Liste / recherche ---
  materiels = signal<MaterielDTO[]>([]);
  totalElements = signal(0);
  page = signal(0);
  readonly pageSize = 24;
  loading = signal(false);

  search = signal('');
  etatFiltre = signal<string>('');
  categorieFiltre = signal<number | null>(null);
  categories = signal<CategorieMateriel[]>([]);
  etablissements = signal<Etablissement[]>([]);

  private searchDebounce: ReturnType<typeof setTimeout> | null = null;

  totalPages = computed(() => Math.max(1, Math.ceil(this.totalElements() / this.pageSize)));

  // --- Détail / drawer ---
  selected = signal<MaterielDTO | null>(null);
  drawerOpen = signal(false);
  activeTab = signal<OngletDetail>('info');
  mouvements = signal<MouvementMateriel[]>([]);
  maintenances = signal<MaintenanceDTO[]>([]);
  detailLoading = signal(false);

  editMode = signal(false);
  editForm: MaterielRequest = this.emptyMaterielRequest();

  showComposantForm = signal(false);
  composantForm: ComposantRequest = { nom: '', quantiteComposant: 1 };

  showMaintenanceForm = signal(false);
  maintenanceForm: MaintenanceRequest = this.emptyMaintenanceRequest(0);

  // --- Création ---
  showCreateModal = signal(false);
  createMode = signal<ModeCreation>('simple');
  simpleForm: MaterielRequest = this.emptyMaterielRequest();
  kitForm: KitRequest = this.emptyKitRequest();
  kitComposants = signal<string[]>([...KIT_COMPOSANTS_DEFAUT]);
  nouveauComposantKit = '';
  saving = signal(false);
  errorMessage = signal<string | null>(null);

  // --- Modal Confirmation Personnalisée ---
  showConfirmModal = signal(false);
  confirmTitle = signal('');
  confirmMessage = signal('');
  private pendingConfirmAction: (() => void) | null = null;

  // --- Achat de stock ---
  showAchatModal = signal(false);
  achatMateriels = signal<MaterielDTO[]>([]);
  achatForm: CreerAchatRequest = this.emptyAchatRequest();
  achatSaving = signal(false);
  achatError = signal<string | null>(null);

  ngOnInit(): void {
    this.materielService.getCategories().subscribe((cats) => this.categories.set(cats));
    this.etablissementService.getAll().subscribe((list) => this.etablissements.set(list));
    this.rechercher();
  }

  // =================================================================
  // Modal de Confirmation Personnalisée
  // =================================================================

  demanderConfirmation(titre: string, message: string, action: () => void): void {
    this.confirmTitle.set(titre);
    this.confirmMessage.set(message);
    this.pendingConfirmAction = action;
    this.showConfirmModal.set(true);
  }

  confirmerAction(): void {
    if (this.pendingConfirmAction) {
      this.pendingConfirmAction();
    }
    this.annulerConfirmation();
  }

  annulerConfirmation(): void {
    this.showConfirmModal.set(false);
    this.confirmTitle.set('');
    this.confirmMessage.set('');
    this.pendingConfirmAction = null;
  }

  // =================================================================
  // Achat de stock
  // =================================================================

  peutAcheter(): boolean {
    return this.authService.hasRole('ADMINISTRATEUR', 'GESTIONNAIRE_STOCK');
  }

  ouvrirAchat(): void {
    this.achatError.set(null);
    this.achatForm = this.emptyAchatRequest();
    this.materielService.rechercher({ topLevelOnly: true, size: 500, page: 0 }).subscribe((res) => {
      this.achatMateriels.set(res.content);
    });
    this.showAchatModal.set(true);
  }

  fermerAchat(): void {
    this.showAchatModal.set(false);
    this.achatError.set(null);
  }

  soumettreAchat(): void {
    this.achatError.set(null);
    if (!this.achatForm.idMateriel || !this.achatForm.quantite || this.achatForm.quantite < 1) {
      this.achatError.set('Matériel et quantité (> 0) sont obligatoires');
      return;
    }
    this.achatSaving.set(true);
    this.achatMaterielService.creer(this.achatForm).subscribe({
      next: () => {
        this.achatSaving.set(false);
        this.fermerAchat();
        this.rechercher(0);
      },
      error: (err) => {
        this.achatError.set(err?.error?.message ?? "Erreur lors de l'enregistrement de l'achat");
        this.achatSaving.set(false);
      },
    });
  }

  private emptyAchatRequest(): CreerAchatRequest {
    return { idMateriel: 0, quantite: 1, fournisseur: '', numeroFacture: '', prixUnitaireHt: null };
  }

  // =================================================================
  // Recherche / filtres
  // =================================================================

  onSearchInput(value: string): void {
    this.search.set(value);
    if (this.searchDebounce) clearTimeout(this.searchDebounce);
    this.searchDebounce = setTimeout(() => this.rechercher(0), 350);
  }

  setEtatFiltre(etat: string): void {
    this.etatFiltre.set(this.etatFiltre() === etat ? '' : etat);
    this.rechercher(0);
  }

  setCategorieFiltre(id: number | null): void {
    this.categorieFiltre.set(id);
    this.rechercher(0);
  }

  rechercher(page: number = this.page()): void {
    this.loading.set(true);
    this.page.set(page);
    const params: RechercheMaterielParams = {
      search: this.search() || undefined,
      etat: this.etatFiltre() || undefined,
      idCategorie: this.categorieFiltre() ?? undefined,
      topLevelOnly: true,
      page,
      size: this.pageSize,
    };
    this.materielService.rechercher(params).subscribe({
      next: (res) => {
        this.materiels.set(res.content);
        this.totalElements.set(res.totalElements);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  allerPage(p: number): void {
    if (p < 0 || p >= this.totalPages()) return;
    this.rechercher(p);
  }

  trackByMateriel(_index: number, m: MaterielDTO): number {
    return m.idMateriel;
  }

  classeEtat(etat?: string | null): string {
    switch (etat) {
      case 'DISPONIBLE': return 'etat-disponible';
      case 'EN_SERVICE': return 'etat-service';
      case 'EN_MAINTENANCE': return 'etat-maintenance';
      case 'RETIRE': return 'etat-retire';
      default: return '';
    }
  }

  // =================================================================
  // Suppressions
  // =================================================================

  supprimerMateriel(m: MaterielDTO, event: Event): void {
    event.stopPropagation();
    this.demanderConfirmation(
      'Supprimer le matériel',
      `Êtes-vous sûr de vouloir supprimer "${m.nom}" (${m.reference}) ?`,
      () => {
        this.materielService.supprimer(m.idMateriel).subscribe({
          next: () => this.rechercher(),
          error: (err) => this.errorMessage.set(err?.error?.message ?? "Erreur lors de la suppression"),
        });
      }
    );
  }

  supprimerDepuisDrawer(): void {
    const m = this.selected();
    if (!m) return;
    this.demanderConfirmation(
      'Supprimer le matériel',
      `Êtes-vous sûr de vouloir supprimer définitivement "${m.nom}" ?`,
      () => {
        this.materielService.supprimer(m.idMateriel).subscribe({
          next: () => {
            this.fermerDrawer();
            this.rechercher();
          },
          error: (err) => this.errorMessage.set(err?.error?.message ?? "Erreur lors de la suppression"),
        });
      }
    );
  }

  // =================================================================
  // Drawer / Détails
  // =================================================================

  ouvrirDetail(m: MaterielDTO): void {
    this.selected.set(m);
    this.drawerOpen.set(true);
    this.activeTab.set('info');
    this.editMode.set(false);
    this.errorMessage.set(null);
    this.chargerDetail(m.idMateriel);
  }

  fermerDrawer(): void {
    this.drawerOpen.set(false);
    this.selected.set(null);
    this.editMode.set(false);
  }

  changerOnglet(onglet: OngletDetail): void {
    this.activeTab.set(onglet);
  }

  chargerDetail(id: number): void {
    this.detailLoading.set(true);
    this.materielService.getById(id).subscribe({
      next: (dto: MaterielDTO) => {
        this.selected.set(dto);
        this.detailLoading.set(false);
      },
      error: () => this.detailLoading.set(false),
    });

    const service = this.materielService as any;
    if (typeof service.getMouvements === 'function') {
      service.getMouvements(id).subscribe({
        next: (list: MouvementMateriel[]) => this.mouvements.set(list),
      });
    }
    if (typeof service.getMaintenances === 'function') {
      service.getMaintenances(id).subscribe({
        next: (list: MaintenanceDTO[]) => this.maintenances.set(list),
      });
    }
  }

  changerEtat(nouvelEtat: string): void {
    const m = this.selected();
    if (!m) return;
    this.materielService.changerEtat(m.idMateriel, nouvelEtat as EtatMateriel).subscribe({
      next: (updated: MaterielDTO) => {
        this.selected.set(updated);
        this.rechercher();
      },
    });
  }

  regenererQr(): void {
    const m = this.selected();
    if (!m) return;
    this.materielService.regenererCodeQr(m.idMateriel).subscribe({
      next: (res: { codeQr: string }) => {
        const current = this.selected();
        if (current) {
          this.selected.set({ ...current, codeQr: res.codeQr });
        }
      },
    });
  }

  entrerEditMode(): void {
    const m = this.selected();
    if (!m) return;
    this.editForm = {
      reference: m.reference,
      nom: m.nom,
      numeroSerie: m.numeroSerie ?? '',
      codeQr: m.codeQr ?? '',
      etat: m.etat ?? undefined,
      idCategorie: m.idCategorie ?? 0,
      idEtablissement: m.idEtablissement ?? null,
    };
    this.editMode.set(true);
  }

  enregistrerEdition(): void {
    const m = this.selected();
    if (!m) return;
    this.saving.set(true);
    this.materielService.modifier(m.idMateriel, this.editForm).subscribe({
      next: (updated: MaterielDTO) => {
        this.selected.set(updated);
        this.editMode.set(false);
        this.saving.set(false);
        this.rechercher();
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message ?? 'Erreur lors de la mise à jour');
        this.saving.set(false);
      },
    });
  }

  // --- Composants (Kits) ---
  ouvrirFormComposant(): void {
    this.composantForm = { nom: '', quantiteComposant: 1 };
    this.showComposantForm.set(true);
  }

  ajouterComposant(): void {
    const m = this.selected();
    if (!m) return;
    this.saving.set(true);
    this.materielService.ajouterComposant(m.idMateriel, this.composantForm).subscribe({
      next: () => {
        this.showComposantForm.set(false);
        this.saving.set(false);
        this.chargerDetail(m.idMateriel);
      },
      error: () => this.saving.set(false),
    });
  }

  retirerComposant(idComposant: number): void {
    const m = this.selected();
    if (!m) return;
    this.materielService.retirerComposant(idComposant).subscribe({
      next: () => this.chargerDetail(m.idMateriel),
    });
  }

  // --- Maintenance ---
  ouvrirFormMaintenance(): void {
    const m = this.selected();
    if (!m) return;
    this.maintenanceForm = this.emptyMaintenanceRequest(m.idMateriel);
    this.showMaintenanceForm.set(true);
  }

  creerMaintenance(): void {
    const m = this.selected();
    if (!m) return;
    this.saving.set(true);
    this.materielService.creerMaintenance(this.maintenanceForm).subscribe({
      next: () => {
        this.showMaintenanceForm.set(false);
        this.saving.set(false);
        this.chargerDetail(m.idMateriel);
      },
      error: () => this.saving.set(false),
    });
  }

  cloturerMaintenance(idMaintenance: number): void {
    const m = this.selected();
    if (!m) return;
    this.materielService.cloturerMaintenance(idMaintenance).subscribe({
      next: () => this.chargerDetail(m.idMateriel),
    });
  }

  // =================================================================
  // Modals Création
  // =================================================================

  ouvrirCreation(mode: ModeCreation = 'simple'): void {
    this.createMode.set(mode);
    this.simpleForm = this.emptyMaterielRequest();
    this.kitForm = this.emptyKitRequest();
    this.kitComposants.set([...KIT_COMPOSANTS_DEFAUT]);
    this.errorMessage.set(null);
    this.showCreateModal.set(true);
  }

  fermerCreation(): void {
    this.showCreateModal.set(false);
    this.errorMessage.set(null);
  }

  categoriesSimples = computed(() =>
    this.categories().filter((c) => !(c as any).estMetacategorie)
  );
  categoriesKit = computed(() =>
    this.categories().filter((c) => (c as any).estMetacategorie)
  );

  ajouterLigneComposantKit(): void {
    const val = this.nouveauComposantKit.trim();
    if (val) {
      this.kitComposants.set([...this.kitComposants(), val]);
      this.nouveauComposantKit = '';
    }
  }

  retirerLigneComposantKit(index: number): void {
    const list = [...this.kitComposants()];
    list.splice(index, 1);
    this.kitComposants.set(list);
  }

  soumettreCreation(): void {
    this.saving.set(true);
    this.errorMessage.set(null);

    if (this.createMode() === 'simple') {
      this.materielService.creerSimple(this.simpleForm).subscribe({
        next: () => {
          this.saving.set(false);
          this.fermerCreation();
          this.rechercher(0);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message ?? 'Erreur lors de la création');
          this.saving.set(false);
        },
      });
    } else {
      this.kitForm.composants = this.kitComposants();
      this.materielService.creerKit(this.kitForm).subscribe({
        next: () => {
          this.saving.set(false);
          this.fermerCreation();
          this.rechercher(0);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message ?? 'Erreur lors de la création du kit');
          this.saving.set(false);
        },
      });
    }
  }

  // --- Helpers ---
  iconePourCategorie(nomCategorie?: string | null): any {
    if (!nomCategorie) return Package;
    const n = nomCategorie.toLowerCase();
    if (n.includes('ordi') || n.includes('laptop')) return Laptop;
    if (n.includes('proj')) return Projector;
    if (n.includes('câble') || n.includes('cable')) return Cable;
    if (n.includes('réseau') || n.includes('router')) return Router;
    return Package;
  }

  private emptyMaterielRequest(): MaterielRequest {
    return {
      reference: '',
      nom: '',
      numeroSerie: '',
      codeQr: '',
      idCategorie: 0,
      idEtablissement: null,
    };
  }

  private emptyKitRequest(): KitRequest {
    return {
      reference: '',
      nom: '',
      numeroSerie: '',
      codeQr: '',
      idCategorie: 0,
      idEtablissement: null,
      composantsHeritentQr: true,
      composants: [],
    };
  }

  private emptyMaintenanceRequest(idMateriel: number): MaintenanceRequest {
    return {
      idMateriel,
      dateMaintenance: new Date().toISOString().split('T')[0],
      description: '',
      cout: null,
      disponible: true,
    };
  }
}