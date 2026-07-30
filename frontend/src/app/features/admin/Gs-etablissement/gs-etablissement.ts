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
  Pencil,
  Trash2,
  X,
  Phone,
  Mail,
  Navigation
} from 'lucide-angular';
import { EtablissementService } from '../../../shared/services/etablissement.service';
import { LocationService } from '../../../shared/services/location.service';
import {
  Commune,
  Etablissement,
  EtablissementKpi,
  EtablissementRequest,
  Province,
  Region
} from '../../../shared/models/etablissement.model';

interface FormModel {
  reference: string;
  designation: string;
  type: string;
  localisationGps: string;
  nombreBeneficiaires: number;
  telephoneContact: string;
  emailContact: string;

  idRegion: number | null;
  idProvince: number | null;
  idCommune: number | null;

  responsableIdResponsable: number | null;
  responsableNom: string;
  responsablePrenom: string;
  responsableFonction: string;
  responsableTelephone: string;
  responsableEmail: string;
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
  private readonly locationService = inject(LocationService);

  readonly icons = {
    Building2, MapPin, Users, UserX, Search, Plus, Download,
    Pencil, Trash2, X, Phone, Mail, Navigation
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

  // --- Modale de confirmation personnalisée ---
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

  // --- Panneau formulaire (création / édition) ---
  readonly showForm = signal<boolean>(false);
  readonly editingId = signal<number | null>(null);
  readonly formModel = signal<FormModel>(this.emptyForm());

  // Listes en cascade pour le formulaire
  readonly formRegions = signal<Region[]>([]);
  readonly formProvinces = signal<Province[]>([]);
  readonly formCommunes = signal<Commune[]>([]);

  private emptyForm(): FormModel {
    return {
      reference: '',
      designation: '',
      type: '',
      localisationGps: '',
      nombreBeneficiaires: 0,
      telephoneContact: '',
      emailContact: '',
      idRegion: null,
      idProvince: null,
      idCommune: null,
      responsableIdResponsable: null,
      responsableNom: '',
      responsablePrenom: '',
      responsableFonction: '',
      responsableTelephone: '',
      responsableEmail: ''
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
  // --- Actions formulaire ---
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
      emailContact: item.emailContact ?? '',
      idRegion: item.idRegion,
      idProvince: item.idProvince,
      idCommune: item.idCommune,
      responsableIdResponsable: item.responsable?.idResponsable ?? null,
      responsableNom: item.responsable?.nom ?? '',
      responsablePrenom: item.responsable?.prenom ?? '',
      responsableFonction: item.responsable?.fonction ?? '',
      responsableTelephone: item.responsable?.telephone ?? '',
      responsableEmail: item.responsable?.email ?? ''
    });

    this.locationService.getProvinces(item.idRegion!).subscribe((provinces) => {
      this.formProvinces.set(provinces);
    });
    this.locationService.getCommunes(item.idProvince!).subscribe((communes) => {
      this.formCommunes.set(communes);
    });
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
      emailContact: m.emailContact || undefined,
      idCommune: m.idCommune,
      responsable: m.responsableNom.trim()
        ? {
            idResponsable: m.responsableIdResponsable,
            nom: m.responsableNom,
            prenom: m.responsablePrenom,
            fonction: m.responsableFonction || undefined,
            telephone: m.responsableTelephone || undefined,
            email: m.responsableEmail || undefined
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

  deleteEtablissement(item: Etablissement, force: boolean = false): void {
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
          // Optionnel : afficher une notification d'erreur si besoin
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
  // --- Divers ---
  // ============================================================

  openOnMap(item: Etablissement): void {
    if (!item.localisationGps) return;
    const [lat, lng] = item.localisationGps.split(',').map((v) => v.trim());
    window.open(`https://www.google.com/maps?q=${lat},${lng}`, '_blank');
  }

  onExport(): void {
    const rows: string[] = [
      'Référence;Désignation;Région;Province;Commune;Bénéficiaires;Responsable;Téléphone;Email'
    ];
    this.filteredEtablissements().forEach((e) => {
      rows.push(
        `${e.reference};${e.designation};${e.regionNom};${e.provinceNom};${e.communeNom};${e.nombreBeneficiaires ?? 0};${e.responsable?.nom ?? ''};${e.responsable?.telephone ?? ''};${e.responsable?.email ?? ''}`
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