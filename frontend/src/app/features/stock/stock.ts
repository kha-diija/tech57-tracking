import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../shared/services/auth.service'; // ⚠️ ajuste le chemin exact selon ton arborescence
import { AchatMaterielService } from './services/achat-materiel.service';
import { CreerAchatRequest } from './models/achat.model';
import { ShoppingCart } from 'lucide-angular'; // nouvelle icône
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

  ngOnInit(): void {
    this.materielService.getCategories().subscribe((cats) => this.categories.set(cats));
    this.rechercher();
  }

// --- Achat de stock ---
showAchatModal = signal(false);
achatMateriels = signal<MaterielDTO[]>([]); // liste complète pour le select
achatForm: CreerAchatRequest = this.emptyAchatRequest();
achatSaving = signal(false);
achatError = signal<string | null>(null);

peutAcheter(): boolean {
  return this.authService.hasRole('ADMINISTRATEUR', 'GESTIONNAIRE_STOCK');
}

ouvrirAchat(): void {
  this.achatError.set(null);
  this.achatForm = this.emptyAchatRequest();
  // Charge une liste complète (sans pagination) pour le select du modal
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
      this.rechercher(0); // rafraîchit la liste (les quantités affichées, si tu les ajoutes plus tard, seront à jour)
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

  // =================================================================
  // Détail (drawer)
  // =================================================================

  ouvrirDetail(m: MaterielDTO): void {
    this.editMode.set(false);
    this.activeTab.set('info');
    this.drawerOpen.set(true);
    this.chargerDetail(m.idMateriel);
  }

  private chargerDetail(id: number): void {
    this.detailLoading.set(true);
    this.materielService.getById(id).subscribe({
      next: (dto) => {
        this.selected.set(dto);
        this.detailLoading.set(false);
      },
      error: () => this.detailLoading.set(false),
    });
  }

  fermerDrawer(): void {
    this.drawerOpen.set(false);
    this.selected.set(null);
    this.showComposantForm.set(false);
    this.showMaintenanceForm.set(false);
  }

  changerOnglet(tab: OngletDetail): void {
    this.activeTab.set(tab);
    const m = this.selected();
    if (!m) return;
    if (tab === 'mouvements') {
      this.materielService.getHistoriqueMouvements(m.idMateriel).subscribe((mv) => this.mouvements.set(mv));
    } else if (tab === 'maintenance') {
      this.materielService.getHistoriqueMaintenance(m.idMateriel).subscribe((mt) => this.maintenances.set(mt));
    }
  }

  // --- Édition ---

  entrerEditMode(): void {
    const m = this.selected();
    if (!m) return;
    this.editForm = {
      reference: m.reference,
      nom: m.nom,
      numeroSerie: m.numeroSerie,
      codeQr: m.codeQr,
      etat: m.etat,
      idCategorie: m.idCategorie!,
      idEtablissement: m.idEtablissement,
    };
    this.editMode.set(true);
  }

  enregistrerEdition(): void {
    const m = this.selected();
    if (!m) return;
    this.saving.set(true);
    this.materielService.modifier(m.idMateriel, this.editForm).subscribe({
      next: (dto) => {
        this.selected.set(dto);
        this.editMode.set(false);
        this.saving.set(false);
        this.rechercher();
      },
      error: (err) => { this.errorMessage.set(err?.error?.message ?? 'Erreur lors de la modification'); this.saving.set(false); },
    });
  }

  changerEtat(etat: EtatMateriel): void {
    const m = this.selected();
    if (!m) return;
    this.materielService.changerEtat(m.idMateriel, etat).subscribe((dto) => {
      this.selected.set(dto);
      this.rechercher();
    });
  }

  supprimerDepuisDrawer(): void {
    const m = this.selected();
    if (!m) return;
    if (!confirm(`Supprimer définitivement "${m.nom}" (${m.reference}) ?`)) return;
    this.materielService.supprimer(m.idMateriel).subscribe(() => {
      this.fermerDrawer();
      this.rechercher();
    });
  }

  regenererQr(): void {
    const m = this.selected();
    if (!m) return;
    this.materielService.regenererCodeQr(m.idMateriel).subscribe((res) => {
      this.selected.update((cur) => (cur ? { ...cur, codeQr: res.codeQr } : cur));
      this.rechercher();
    });
  }

  // --- Composants de kit ---

  ouvrirFormComposant(): void {
    this.composantForm = { nom: '', quantiteComposant: 1 };
    this.showComposantForm.set(true);
  }

  ajouterComposant(): void {
    const m = this.selected();
    if (!m || !this.composantForm.nom.trim()) return;
    this.saving.set(true);
    this.materielService.ajouterComposant(m.idMateriel, this.composantForm).subscribe({
      next: (dto) => {
        this.selected.set(dto);
        this.showComposantForm.set(false);
        this.saving.set(false);
      },
      error: (err) => { this.errorMessage.set(err?.error?.message ?? "Erreur lors de l'ajout du composant"); this.saving.set(false); },
    });
  }

  retirerComposant(idComposant: number): void {
    if (!confirm('Retirer ce composant du kit ?')) return;
    this.materielService.retirerComposant(idComposant).subscribe(() => {
      const m = this.selected();
      if (m) this.chargerDetail(m.idMateriel);
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
    this.saving.set(true);
    this.materielService.creerMaintenance(this.maintenanceForm).subscribe({
      next: () => {
        this.showMaintenanceForm.set(false);
        this.saving.set(false);
        const m = this.selected();
        if (m) {
          this.chargerDetail(m.idMateriel);
          this.materielService.getHistoriqueMaintenance(m.idMateriel).subscribe((mt) => this.maintenances.set(mt));
        }
        this.rechercher();
      },
      error: (err) => { this.errorMessage.set(err?.error?.message ?? "Erreur lors de l'ajout de la maintenance"); this.saving.set(false); },
    });
  }

  cloturerMaintenance(id: number): void {
    this.materielService.cloturerMaintenance(id).subscribe(() => {
      const m = this.selected();
      if (m) {
        this.chargerDetail(m.idMateriel);
        this.materielService.getHistoriqueMaintenance(m.idMateriel).subscribe((mt) => this.maintenances.set(mt));
      }
      this.rechercher();
    });
  }

  // =================================================================
  // Création (simple / kit)
  // =================================================================

  ouvrirCreation(mode: ModeCreation): void {
    this.errorMessage.set(null);
    this.createMode.set(mode);
    this.simpleForm = this.emptyMaterielRequest();
    this.kitForm = this.emptyKitRequest();
    this.kitComposants.set([...KIT_COMPOSANTS_DEFAUT]);
    this.showCreateModal.set(true);
  }

  fermerCreation(): void {
    this.showCreateModal.set(false);
    this.errorMessage.set(null);
  }

  ajouterLigneComposantKit(): void {
    const nom = this.nouveauComposantKit.trim();
    if (!nom) return;
    this.kitComposants.update((list) => [...list, nom]);
    this.nouveauComposantKit = '';
  }

  retirerLigneComposantKit(index: number): void {
    this.kitComposants.update((list) => list.filter((_, i) => i !== index));
  }

  soumettreCreation(): void {
    this.errorMessage.set(null);
    if (this.createMode() === 'simple') {
      if (!this.simpleForm.reference.trim() || !this.simpleForm.nom.trim() || !this.simpleForm.idCategorie) {
        this.errorMessage.set('Référence, nom et catégorie sont obligatoires');
        return;
      }
      this.saving.set(true);
      this.materielService.creerSimple(this.simpleForm).subscribe({
        next: () => { this.saving.set(false); this.fermerCreation(); this.rechercher(0); },
        error: (err) => { this.errorMessage.set(err?.error?.message ?? 'Erreur lors de la création'); this.saving.set(false); },
      });
    } else {
      if (!this.kitForm.reference.trim() || !this.kitForm.nom.trim() || !this.kitForm.idCategorie) {
        this.errorMessage.set('Référence, nom et catégorie sont obligatoires');
        return;
      }
      this.kitForm.composants = this.kitComposants();
      this.saving.set(true);
      this.materielService.creerKit(this.kitForm).subscribe({
        next: () => { this.saving.set(false); this.fermerCreation(); this.rechercher(0); },
        error: (err) => { this.errorMessage.set(err?.error?.message ?? 'Erreur lors de la création du kit'); this.saving.set(false); },
      });
    }
  }

  categoriesKit = computed(() => this.categories().filter((c) => c.estKit));
  categoriesSimples = computed(() => this.categories().filter((c) => !c.estKit));

  // =================================================================
  // Helpers d'affichage
  // =================================================================

  iconePourCategorie(nomCategorie: string | null): any {
    const n = (nomCategorie ?? '').toLowerCase();
    if (n.includes('pc') || n.includes('portable')) return this.icons.Laptop;
    if (n.includes('datashow') || n.includes('vidéoprojecteur') || n.includes('videoprojecteur')) return this.icons.Projector;
    if (n.includes('rallonge') || n.includes('câble') || n.includes('cable')) return this.icons.Cable;
    if (n.includes('routeur') || n.includes('wi-fi') || n.includes('wifi')) return this.icons.Router;
    if (n.includes('kit')) return this.icons.Boxes;
    return this.icons.Package;
  }

  classeEtat(etat: string): string {
    switch (etat) {
      case 'Neuf': return 'etat-neuf';
      case 'En service': return 'etat-service';
      case 'En panne': return 'etat-panne';
      case 'Retiré': return 'etat-retire';
      default: return '';
    }
  }

  private emptyMaterielRequest(): MaterielRequest {
    return { reference: '', nom: '', numeroSerie: '', codeQr: '', etat: 'Neuf', idCategorie: 0, idEtablissement: null };
  }

  private emptyKitRequest(): KitRequest {
    return { reference: '', nom: '', numeroSerie: '', codeQr: '', idCategorie: 0, idEtablissement: null, composantsHeritentQr: true };
  }

  private emptyMaintenanceRequest(idMateriel: number): MaintenanceRequest {
    return { dateMaintenance: new Date().toISOString().slice(0, 10), description: '', cout: null, disponible: false, idMateriel };
  }

  trackByMateriel(_: number, m: MaterielDTO): number {
    return m.idMateriel;
  }
}