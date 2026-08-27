import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  LucideAngularModule,
  Building2,
  MapPin,
  Users,
  UserX,
  Search,
  Plus,
  Download,
  Upload,
  Pencil,
  Trash2,
  X,
  Phone,
  Navigation,
  GraduationCap,
   CheckCircle 
} from 'lucide-angular';
import { EtablissementService } from '../../../shared/services/etablissement.service';
import { FormateurService } from '../../../shared/services/formateur.service';
import { LocationService } from '../../../shared/services/location.service';
import { AuthService } from '../../../shared/services/auth.service';
import {
  Commune,
  Etablissement,
  EtablissementKpi,
  EtablissementRequest,
  Province,
  Region,
  Formateur,
  FormateurRequest,
  ImportResult
} from '../../../shared/models/etablissement.model';

interface FormModel {
  reference: string;
  designation: string;
  type: string;
  localisationGps: string;
  nombreBeneficiaires: number;
  telephoneContact: string;

  idRegion: number | null;
  idProvince: number | null;
  idCommune: number | null;

  responsableIdResponsable: number | null;
  responsableNom: string;
  responsablePrenom: string;
  responsableFonction: string;
  responsableTelephone: string;
}

interface FormateurModalState {
  isOpen: boolean;
  etablissement: Etablissement | null;
  formateurs: Formateur[];
  isLoading: boolean;
  editingId: number | null;
  form: FormateurRequest;
}

interface ImportModalState {
  isOpen: boolean;
  selectedFile: File | null;
  idProvince: number | null;
  isImporting: boolean;
  result: ImportResult | null;
  error: string | null;
}

@Component({
  selector: 'app-gs-etablissement',
  standalone: true,
  imports: [CommonModule, FormsModule, LucideAngularModule],
  templateUrl: './gs-etablissement.html',
  styleUrl: './gs-etablissement.scss'
})
export class GsEtablissement {
  private readonly etablissementService = inject(EtablissementService);
  private readonly formateurService = inject(FormateurService);
  private readonly locationService = inject(LocationService);
  private readonly authService = inject(AuthService);

  // Vérifie si l'utilisateur connecté est un administrateur
  readonly isAdmin = computed(() => {
    return this.authService.hasRole('ADMINISTRATEUR');
  });

  readonly icons = {
  Building2, MapPin, Users, UserX, Search, Plus, Download, Upload,
  Pencil, Trash2, X, Phone, Navigation, GraduationCap, CheckCircle
};

  readonly typeOptions = ['École', 'Collège', 'Lycée', 'Université', 'Centre de formation', 'Autre'];

  // --- Données ---
  readonly etablissements = signal<Etablissement[]>([]);
  readonly kpis = signal<EtablissementKpi | null>(null);
  readonly isLoading = signal<boolean>(true);

  // --- Filtre région (toolbar) ---
  readonly regionsFilterList = signal<Region[]>([]);
  readonly selectedRegionFilter = signal<string>('toutes');
  readonly searchTerm = signal<string>('');

  // --- Modale de suppression (réservée à l'admin) ---
  readonly confirmModal = signal<{
    isOpen: boolean;
    title: string;
    message: string;
    isError?: boolean;
    item: Etablissement | null;
    force: boolean;
  }>({
    isOpen: false,
    title: '',
    message: '',
    isError: false,
    item: null,
    force: false
  });

  readonly filteredEtablissements = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const region = this.selectedRegionFilter();

    return this.etablissements().filter((e) => {
      const matchesRegion = region === 'toutes' || e.regionNom === region;
      const matchesTerm =
        !term ||
        e.designation.toLowerCase().includes(term) ||
        e.reference.toLowerCase().includes(term) ||
        e.communeNom.toLowerCase().includes(term) ||
        (e.responsable?.nom ?? '').toLowerCase().includes(term);
      return matchesRegion && matchesTerm;
    });
  });

  // --- Panneau formulaire (création / édition accessible à tous les deux) ---
  readonly showForm = signal<boolean>(false);
  readonly editingId = signal<number | null>(null);
  readonly formModel = signal<FormModel>(this.emptyForm());

  // Listes en cascade pour le formulaire
  readonly formRegions = signal<Region[]>([]);
  readonly formProvinces = signal<Province[]>([]);
  readonly formCommunes = signal<Commune[]>([]);

  // --- Modale de gestion des formateurs (ex-Observateur côté backend) ---
  readonly formateurModal = signal<FormateurModalState>(this.emptyFormateurModal());

  // --- Modale d'import Excel ---
  readonly importModal = signal<ImportModalState>(this.emptyImportModal());
  readonly importProvinces = signal<Province[]>([]);

  private emptyForm(): FormModel {
    return {
      reference: '',
      designation: '',
      type: '',
      localisationGps: '',
      nombreBeneficiaires: 0,
      telephoneContact: '',
      idRegion: null,
      idProvince: null,
      idCommune: null,
      responsableIdResponsable: null,
      responsableNom: '',
      responsablePrenom: '',
      responsableFonction: '',
      responsableTelephone: ''
    };
  }

  private emptyFormateurModal(): FormateurModalState {
    return {
      isOpen: false,
      etablissement: null,
      formateurs: [],
      isLoading: false,
      editingId: null,
      form: { nom: '', prenom: '', telephone: '', adresse: '' }
    };
  }

  private emptyImportModal(): ImportModalState {
    return {
      isOpen: false,
      selectedFile: null,
      idProvince: null,
      isImporting: false,
      result: null,
      error: null
    };
  }

  constructor() {
    this.loadData();
    this.locationService.getRegions().subscribe((regions) => {
      this.regionsFilterList.set(regions);
      this.formRegions.set(regions);
    });
  }

  private loadData(): void {
    this.isLoading.set(true);
    this.etablissementService.getAll().subscribe((data) => {
      this.etablissements.set(data);
      this.isLoading.set(false);
    });
    this.etablissementService.getKpis().subscribe((data) => this.kpis.set(data));
  }

  // ============================================================
  // --- Cascade région → province → commune (formulaire) ---
  // ============================================================

  onFormRegionChange(idRegion: number | string): void {
    const id = Number(idRegion) || null;
    this.formModel.update((m) => ({ ...m, idRegion: id, idProvince: null, idCommune: null }));
    this.formProvinces.set([]);
    this.formCommunes.set([]);
    if (id) {
      this.locationService.getProvinces(id).subscribe((provinces) => this.formProvinces.set(provinces));
    }
  }

  onFormProvinceChange(idProvince: number | string): void {
    const id = Number(idProvince) || null;
    this.formModel.update((m) => ({ ...m, idProvince: id, idCommune: null }));
    this.formCommunes.set([]);
    if (id) {
      this.locationService.getCommunes(id).subscribe((communes) => this.formCommunes.set(communes));
    }
  }

  onFormCommuneChange(idCommune: number | string): void {
    this.formModel.update((m) => ({ ...m, idCommune: Number(idCommune) || null }));
  }

  updateField<K extends keyof FormModel>(field: K, value: FormModel[K]): void {
    this.formModel.update((m) => ({ ...m, [field]: value }));
  }

  // ============================================================
  // --- Actions formulaire (Créer / Modifier accessible au technicien et admin) ---
  // ============================================================

  openCreateForm(): void {
    this.editingId.set(null);
    this.formModel.set(this.emptyForm());
    this.formProvinces.set([]);
    this.formCommunes.set([]);
    this.showForm.set(true);
  }

  openEditForm(item: Etablissement): void {
    this.editingId.set(item.idEtablissement);
    this.showForm.set(true);

    this.formModel.set({
      reference: item.reference,
      designation: item.designation,
      type: item.type,
      localisationGps: item.localisationGps ?? '',
      nombreBeneficiaires: item.nombreBeneficiaires ?? 0,
      telephoneContact: item.telephoneContact ?? '',
      idRegion: item.idRegion,
      idProvince: item.idProvince,
      idCommune: item.idCommune,
      responsableIdResponsable: item.responsable?.idResponsable ?? null,
      responsableNom: item.responsable?.nom ?? '',
      responsablePrenom: item.responsable?.prenom ?? '',
      responsableFonction: item.responsable?.fonction ?? '',
      responsableTelephone: item.responsable?.telephone ?? ''
    });

    if (item.idRegion) {
      this.locationService.getProvinces(item.idRegion).subscribe((provinces) => {
        this.formProvinces.set(provinces);
      });
    }
    if (item.idProvince) {
      this.locationService.getCommunes(item.idProvince).subscribe((communes) => {
        this.formCommunes.set(communes);
      });
    }
  }

  closeForm(): void {
    this.showForm.set(false);
  }

  saveForm(): void {
    const m = this.formModel();
    if (!m.designation.trim() || !m.reference.trim() || !m.idCommune) return;

    const payload: EtablissementRequest = {
      reference: m.reference,
      designation: m.designation,
      type: m.type,
      localisationGps: m.localisationGps || undefined,
      nombreBeneficiaires: m.nombreBeneficiaires,
      telephoneContact: m.telephoneContact || undefined,
      idCommune: m.idCommune,
      responsable: m.responsableNom.trim()
        ? {
            idResponsable: m.responsableIdResponsable,
            nom: m.responsableNom,
            prenom: m.responsablePrenom,
            fonction: m.responsableFonction || undefined,
            telephone: m.responsableTelephone || undefined
          }
        : null
    };

    const id = this.editingId();
    const request = id
      ? this.etablissementService.update(id, payload)
      : this.etablissementService.create(payload);

    request.subscribe(() => {
      this.closeForm();
      this.loadData();
    });
  }

  // ============================================================
  // --- Suppression (Strictement réservée à l'Administrateur) ---
  // ============================================================

  deleteEtablissement(item: Etablissement, force: boolean = false): void {
    if (!this.isAdmin()) return; // Sécurité de blocage si ce n'est pas l'admin

    if (!force) {
      this.confirmModal.set({
        isOpen: true,
        title: "Confirmer la suppression",
        message: `Supprimer "${item.designation}" ? Cette action est irréversible.`,
        isError: false,
        item: item,
        force: false
      });
      return;
    }

    this.etablissementService.delete(item.idEtablissement, force).subscribe({
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
    if (!this.isAdmin()) return;

    const modal = this.confirmModal();
    if (!modal.item) {
      this.closeConfirmModal();
      return;
    }

    const item = modal.item;
    const force = modal.force;

    this.etablissementService.delete(item.idEtablissement, force).subscribe({
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
    this.confirmModal.set({
      isOpen: false,
      title: '',
      message: '',
      isError: false,
      item: null,
      force: false
    });
  }

  // ============================================================
  // --- Formateurs (entité "Observateur" côté backend) ---
  // ============================================================

  openFormateurModal(etab: Etablissement): void {
    this.formateurModal.set({
      isOpen: true,
      etablissement: etab,
      formateurs: [],
      isLoading: true,
      editingId: null,
      form: { nom: '', prenom: '', telephone: '', adresse: '' }
    });

    this.formateurService.getByEtablissement(etab.idEtablissement).subscribe((list) => {
      this.formateurModal.update((m) => ({ ...m, formateurs: list, isLoading: false }));
    });
  }

  closeFormateurModal(): void {
    this.formateurModal.set(this.emptyFormateurModal());
  }

  updateFormateurField<K extends keyof FormateurRequest>(field: K, value: FormateurRequest[K]): void {
    this.formateurModal.update((m) => ({ ...m, form: { ...m.form, [field]: value } }));
  }

  editFormateur(f: Formateur): void {
    this.formateurModal.update((m) => ({
      ...m,
      editingId: f.idFormateur,
      form: {
        nom: f.nom,
        prenom: f.prenom,
        telephone: f.telephone ?? '',
        adresse: f.adresse ?? ''
      }
    }));
  }

  cancelEditFormateur(): void {
    this.formateurModal.update((m) => ({
      ...m,
      editingId: null,
      form: { nom: '', prenom: '', telephone: '', adresse: '' }
    }));
  }

  saveFormateur(): void {
    const modal = this.formateurModal();
    const etab = modal.etablissement;
    if (!etab || !modal.form.nom.trim() || !modal.form.prenom.trim()) return;

    const isCreate = !modal.editingId;
    const request = modal.editingId
      ? this.formateurService.update(etab.idEtablissement, modal.editingId, modal.form)
      : this.formateurService.create(etab.idEtablissement, modal.form);

    request.subscribe(() => {
      this.formateurService.getByEtablissement(etab.idEtablissement).subscribe((list) => {
        this.formateurModal.update((m) => ({
          ...m,
          formateurs: list,
          editingId: null,
          form: { nom: '', prenom: '', telephone: '', adresse: '' }
        }));
      });

      if (isCreate) {
        this.syncEtablissementFormateurCount(etab.idEtablissement, 1);
      }
    });
  }

  deleteFormateur(f: Formateur): void {
    const etab = this.formateurModal().etablissement;
    if (!etab) return;

    this.formateurService.delete(etab.idEtablissement, f.idFormateur).subscribe(() => {
      this.formateurModal.update((m) => ({
        ...m,
        formateurs: m.formateurs.filter((x) => x.idFormateur !== f.idFormateur)
      }));
      this.syncEtablissementFormateurCount(etab.idEtablissement, -1);
    });
  }

  private syncEtablissementFormateurCount(idEtablissement: number, delta: number): void {
    this.etablissements.update((list) =>
      list.map((e) =>
        e.idEtablissement === idEtablissement
          ? { ...e, nbFormateurs: Math.max(0, (e.nbFormateurs ?? 0) + delta) }
          : e
      )
    );
  }

  // ============================================================
  // --- Import Excel ---
  // ============================================================

  openImportModal(): void {
    this.importModal.set(this.emptyImportModal());
    this.importModal.update((m) => ({ ...m, isOpen: true }));
    if (this.importProvinces().length === 0) {
      this.locationService.getProvinces().subscribe((provinces) => this.importProvinces.set(provinces));
    }
  }

  closeImportModal(): void {
    this.importModal.set(this.emptyImportModal());
  }

  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.importModal.update((m) => ({ ...m, selectedFile: file, error: null }));
  }

  onImportProvinceChange(idProvince: number | string): void {
    this.importModal.update((m) => ({ ...m, idProvince: Number(idProvince) || null }));
  }

  submitImport(): void {
    const m = this.importModal();
    if (!m.selectedFile || !m.idProvince) {
      this.importModal.update((s) => ({ ...s, error: 'Sélectionnez un fichier et une province.' }));
      return;
    }

    this.importModal.update((s) => ({ ...s, isImporting: true, error: null }));

    this.etablissementService.importExcel(m.selectedFile, m.idProvince).subscribe({
      next: (result) => {
        this.importModal.update((s) => ({ ...s, isImporting: false, result }));
        this.loadData();
      },
      error: () => {
        this.importModal.update((s) => ({
          ...s,
          isImporting: false,
          error: "Échec de l'import. Vérifiez le fichier et réessayez."
        }));
      }
    });
  }

  // ============================================================
  // --- Divers ---
  // ============================================================

  openOnMap(item: Etablissement): void {
    if (!item.localisationGps) return;
    const [lat, lng] = item.localisationGps.split(',').map((v) => v.trim());
    window.open(`https://www.google.com/maps/search/?api=1&query=${lat},${lng}`, '_blank');
  }

  onExport(): void {
    const rows: string[] = [
      'Référence;Désignation;Région;Province;Commune;Bénéficiaires;Responsable;Téléphone;Formateurs'
    ];
    this.filteredEtablissements().forEach((e) => {
      rows.push(
        `${e.reference};${e.designation};${e.regionNom};${e.provinceNom};${e.communeNom};${e.nombreBeneficiaires ?? 0};${e.responsable?.nom ?? ''};${e.responsable?.telephone ?? ''};${e.nbFormateurs ?? 0}`
      );
    });

    const csvContent = rows.join('\n');
    const blob = new Blob([`\uFEFF${csvContent}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `etablissements-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }
}