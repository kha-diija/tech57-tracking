import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ObservateurPermissionService } from '../../shared/services/observateur-permission.service';
import { EtablissementService } from '../../shared/services/etablissement.service';
import { Etablissement } from '../../shared/models/etablissement.model';
import {
  ObservateurSummary,
  VideoAssignment,
  ResourceAssignment,
  DocumentAssignment,
  VideoCatalogItem,
  RessourceCatalogItem,
  DocumentCatalogItem,
  CreateVideoRequest,
} from '../../shared/models/permission.model';

@Component({
  selector: 'app-observateur',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './observateur.html',
  styleUrl: './observateur.scss',
})
export class ObservateurPermissions implements OnInit {
  private permissionService = inject(ObservateurPermissionService);
  private etablissementService = inject(EtablissementService);

  // Liste principale
  observateurs = signal<ObservateurSummary[]>([]);
  loading = signal<boolean>(false);
  errorMessage = signal<string>('');
  searchQuery = signal<string>('');

  filteredObservateurs = computed(() => {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) return this.observateurs();
    return this.observateurs().filter((o) =>
      `${o.nom} ${o.prenom} ${o.email}`.toLowerCase().includes(query)
    );
  });

  // Modal assignation
  isModalOpen = signal<boolean>(false);
  selectedObservateur = signal<ObservateurSummary | null>(null);
  modalLoading = signal<boolean>(false);
  modalError = signal<string>('');

  assignedVideos = signal<VideoAssignment[]>([]);
  assignedResources = signal<ResourceAssignment[]>([]);
  assignedDocuments = signal<DocumentAssignment[]>([]);

  videoCatalog = signal<VideoCatalogItem[]>([]);
  resourceCatalog = signal<RessourceCatalogItem[]>([]);
  documentCatalog = signal<DocumentCatalogItem[]>([]);

  selectedVideoId: number | null = null;
  selectedResourceId: number | null = null;
  selectedDocumentId: number | null = null;

  assigningVideo = signal<boolean>(false);
  assigningResource = signal<boolean>(false);
  assigningDocument = signal<boolean>(false);

  availableVideos = computed(() => {
    const assignedIds = new Set(this.assignedVideos().map((a) => a.idVideo));
    return this.videoCatalog().filter((v) => !assignedIds.has(v.idVideo));
  });

  availableResources = computed(() => {
    const assignedIds = new Set(this.assignedResources().map((a) => a.idRessource));
    return this.resourceCatalog().filter((r) => !assignedIds.has(r.idRessource));
  });

  availableDocuments = computed(() => {
    const assignedIds = new Set(this.assignedDocuments().map((a) => a.idDocument));
    return this.documentCatalog().filter((d) => !assignedIds.has(d.idSource));
  });

  // ----------------------------------------------------------
  // Modal d'upload de ressources
  // ----------------------------------------------------------
  isUploadModalOpen = signal<boolean>(false);
  uploadTab = signal<'document' | 'ressource' | 'video'>('document');
  uploading = signal<boolean>(false);
  uploadError = signal<string>('');
  uploadSuccess = signal<string>('');

  selectedFile: File | null = null;

  // TODO: vérifier le nom exact des champs (idEtablissement/designation) une fois
  // etablissement.model.ts fourni.
  etablissements = signal<Etablissement[]>([]);

  docForm = { typeSource: 'PDF' };
  ressourceForm: { titre: string; type: string; idEtablissement: number | null } = {
    titre: '',
    type: 'Guide',
    idEtablissement: null,
  };
  videoForm: CreateVideoRequest = {
    titre: '',
    urlVideo: '',
    description: '',
    urlMiniature: '',
    dureeSecondes: undefined,
  };

  ngOnInit(): void {
    this.loadObservateurs();
  }

  loadObservateurs(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.permissionService.getObservateurs().subscribe({
      next: (data) => {
        this.observateurs.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err?.error?.message || 'Erreur lors du chargement des observateurs.');
        this.loading.set(false);
      },
    });
  }

  // ---- Modal assignation ----
  openModal(observateur: ObservateurSummary): void {
    this.selectedObservateur.set(observateur);
    this.modalError.set('');
    this.selectedVideoId = null;
    this.selectedResourceId = null;
    this.selectedDocumentId = null;
    this.isModalOpen.set(true);
    this.loadModalData(observateur.id);
  }

  closeModal(): void {
    this.isModalOpen.set(false);
    this.selectedObservateur.set(null);
  }

  private loadModalData(idObservateur: number): void {
    this.modalLoading.set(true);
    this.modalError.set('');

    this.permissionService.getVideosAssignedTo(idObservateur).subscribe({
      next: (data) => this.assignedVideos.set(data),
      error: () => this.modalError.set('Erreur lors du chargement des vidéos assignées.'),
    });

    this.permissionService.getResourcesAssignedTo(idObservateur).subscribe({
      next: (data) => this.assignedResources.set(data),
      error: () => this.modalError.set('Erreur lors du chargement des ressources assignées.'),
    });

    this.permissionService.getDocumentsAssignedTo(idObservateur).subscribe({
      next: (data) => this.assignedDocuments.set(data),
      error: () => this.modalError.set('Erreur lors du chargement des documents assignés.'),
    });

    this.permissionService.getVideoCatalog().subscribe({
      next: (data) => this.videoCatalog.set(data),
      error: () => this.modalError.set('Erreur lors du chargement du catalogue vidéos.'),
    });

    this.permissionService.getResourceCatalog().subscribe({
      next: (data) => this.resourceCatalog.set(data),
      error: () => this.modalError.set('Erreur lors du chargement du catalogue ressources.'),
    });

    this.permissionService.getDocumentCatalog().subscribe({
      next: (data) => {
        this.documentCatalog.set(data);
        this.modalLoading.set(false);
      },
      error: () => {
        this.modalError.set('Erreur lors du chargement du catalogue documents.');
        this.modalLoading.set(false);
      },
    });
  }

  // ---- Assigner ----
  assignVideo(): void {
    const observateur = this.selectedObservateur();
    if (!observateur || this.selectedVideoId == null) return;

    this.assigningVideo.set(true);
    this.permissionService
      .assignVideo({ idObservateur: observateur.id, idVideo: this.selectedVideoId })
      .subscribe({
        next: (created) => {
          this.assignedVideos.update((list) => [...list, created]);
          this.selectedVideoId = null;
          this.assigningVideo.set(false);
        },
        error: (err) => {
          this.modalError.set(err?.error?.message || "Erreur lors de l'assignation de la vidéo.");
          this.assigningVideo.set(false);
        },
      });
  }

  assignResource(): void {
    const observateur = this.selectedObservateur();
    if (!observateur || this.selectedResourceId == null) return;

    this.assigningResource.set(true);
    this.permissionService
      .assignResource({ idObservateur: observateur.id, idRessource: this.selectedResourceId })
      .subscribe({
        next: (created) => {
          this.assignedResources.update((list) => [...list, created]);
          this.selectedResourceId = null;
          this.assigningResource.set(false);
        },
        error: (err) => {
          this.modalError.set(err?.error?.message || "Erreur lors de l'assignation de la ressource.");
          this.assigningResource.set(false);
        },
      });
  }

  assignDocument(): void {
    const observateur = this.selectedObservateur();
    if (!observateur || this.selectedDocumentId == null) return;

    this.assigningDocument.set(true);
    this.permissionService
      .assignDocument({ idObservateur: observateur.id, idDocument: this.selectedDocumentId })
      .subscribe({
        next: (created) => {
          this.assignedDocuments.update((list) => [...list, created]);
          this.selectedDocumentId = null;
          this.assigningDocument.set(false);
        },
        error: (err) => {
          this.modalError.set(err?.error?.message || "Erreur lors de l'assignation du document.");
          this.assigningDocument.set(false);
        },
      });
  }

  // ---- Révoquer ----
  revokeVideo(assignment: VideoAssignment): void {
    const observateur = this.selectedObservateur();
    if (!observateur) return;
    if (!confirm(`Retirer l'accès à "${assignment.titreVideo}" ?`)) return;

    this.permissionService.revokeVideo(observateur.id, assignment.idVideo).subscribe({
      next: () => {
        this.assignedVideos.update((list) => list.filter((v) => v.idVideo !== assignment.idVideo));
      },
      error: (err) => {
        this.modalError.set(err?.error?.message || 'Erreur lors de la révocation.');
      },
    });
  }

  revokeResource(assignment: ResourceAssignment): void {
    const observateur = this.selectedObservateur();
    if (!observateur) return;
    if (!confirm(`Retirer l'accès à "${assignment.titreRessource}" ?`)) return;

    this.permissionService.revokeResource(observateur.id, assignment.idRessource).subscribe({
      next: () => {
        this.assignedResources.update((list) => list.filter((r) => r.idRessource !== assignment.idRessource));
      },
      error: (err) => {
        this.modalError.set(err?.error?.message || 'Erreur lors de la révocation.');
      },
    });
  }

  revokeDocument(assignment: DocumentAssignment): void {
    const observateur = this.selectedObservateur();
    if (!observateur) return;
    if (!confirm(`Retirer l'accès à "${assignment.nomFichier}" ?`)) return;

    this.permissionService.revokeDocument(observateur.id, assignment.idDocument).subscribe({
      next: () => {
        this.assignedDocuments.update((list) => list.filter((d) => d.idDocument !== assignment.idDocument));
      },
      error: (err) => {
        this.modalError.set(err?.error?.message || 'Erreur lors de la révocation.');
      },
    });
  }

  // ----------------------------------------------------------
  // Upload de ressources (document / ressource / vidéo)
  // ----------------------------------------------------------
  openUploadModal(): void {
    this.uploadError.set('');
    this.uploadSuccess.set('');
    this.selectedFile = null;
    this.docForm = { typeSource: 'PDF' };
    this.ressourceForm = { titre: '', type: 'Guide', idEtablissement: null };
    this.videoForm = { titre: '', urlVideo: '', description: '', urlMiniature: '', dureeSecondes: undefined };
    this.isUploadModalOpen.set(true);

    // Charge la liste des établissements pour le select "ressource"
    this.etablissementService.getAll().subscribe({
      next: (data) => this.etablissements.set(data),
      error: () => this.uploadError.set('Erreur lors du chargement des établissements.'),
    });
  }

  closeUploadModal(): void {
    this.isUploadModalOpen.set(false);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  submitDocument(): void {
    if (!this.selectedFile) {
      this.uploadError.set('Sélectionnez un fichier.');
      return;
    }
    this.uploading.set(true);
    this.uploadError.set('');
    this.permissionService.uploadDocument(this.selectedFile, this.docForm.typeSource).subscribe({
      next: (res) => {
        this.documentCatalog.update((list) => [
          ...list,
          { idSource: res.idSource, nomFichier: res.nomFichier, typeSource: res.typeSource },
        ]);
        this.uploadSuccess.set(`Document "${res.nomFichier}" ajouté.`);
        this.uploading.set(false);
        this.selectedFile = null;
      },
      error: (err) => {
        this.uploadError.set(err?.error?.message || "Erreur lors de l'upload du document.");
        this.uploading.set(false);
      },
    });
  }

  submitRessource(): void {
    if (!this.selectedFile || !this.ressourceForm.titre.trim()) {
      this.uploadError.set('Titre et fichier requis.');
      return;
    }
    if (this.ressourceForm.idEtablissement == null) {
      this.uploadError.set("L'établissement est obligatoire.");
      return;
    }

    this.uploading.set(true);
    this.uploadError.set('');
    this.permissionService
      .uploadRessource(
        this.selectedFile,
        this.ressourceForm.titre,
        this.ressourceForm.type,
        this.ressourceForm.idEtablissement
      )
      .subscribe({
        next: (res) => {
          this.resourceCatalog.update((list) => [
            ...list,
            { idRessource: res.idRessource, titre: res.titre, type: res.type },
          ]);
          this.uploadSuccess.set(`Ressource "${res.titre}" ajoutée.`);
          this.uploading.set(false);
          this.selectedFile = null;
        },
        error: (err) => {
          this.uploadError.set(err?.error?.message || "Erreur lors de l'upload de la ressource.");
          this.uploading.set(false);
        },
      });
  }

  submitVideo(): void {
    if (!this.videoForm.titre.trim() || !this.videoForm.urlVideo.trim()) {
      this.uploadError.set('Titre et URL vidéo requis.');
      return;
    }
    this.uploading.set(true);
    this.uploadError.set('');
    this.permissionService.createVideo(this.videoForm).subscribe({
      next: (res) => {
        this.videoCatalog.update((list) => [...list, res]);
        this.uploadSuccess.set(`Vidéo "${res.titre}" ajoutée.`);
        this.uploading.set(false);
      },
      error: (err) => {
        this.uploadError.set(err?.error?.message || "Erreur lors de la création de la vidéo.");
        this.uploading.set(false);
      },
    });
  }
}