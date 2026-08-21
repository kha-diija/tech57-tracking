package com.example.backend.service.admin;

import com.example.backend.dto.admin.upload.CreateVideoRequest;
import com.example.backend.dto.admin.upload.DocumentUploadResponse;
import com.example.backend.dto.admin.upload.RessourceUploadResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.AdministrateurRepository;
import com.example.backend.repository.DocumentSourceRepository;
import com.example.backend.repository.RessourceInstallationRepository;
import com.example.backend.repository.VideoMaterielRepository;
import com.example.backend.repository.admin.EtablissementRepository;
import com.example.backend.service.storage.FileStorageService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class AdminResourceUploadService {

    private final FileStorageService fileStorageService;
    private final DocumentSourceRepository documentSourceRepository;
    private final RessourceInstallationRepository ressourceInstallationRepository;
    private final VideoMaterielRepository videoMaterielRepository;
    private final EtablissementRepository etablissementRepository;
    private final AdministrateurRepository administrateurRepository;
    private final EntityManager entityManager;

    public AdminResourceUploadService(
            FileStorageService fileStorageService,
            DocumentSourceRepository documentSourceRepository,
            RessourceInstallationRepository ressourceInstallationRepository,
            VideoMaterielRepository videoMaterielRepository,
            EtablissementRepository etablissementRepository,
            AdministrateurRepository administrateurRepository,
            EntityManager entityManager) {
        this.fileStorageService = fileStorageService;
        this.documentSourceRepository = documentSourceRepository;
        this.ressourceInstallationRepository = ressourceInstallationRepository;
        this.videoMaterielRepository = videoMaterielRepository;
        this.etablissementRepository = etablissementRepository;
        this.administrateurRepository = administrateurRepository;
        this.entityManager = entityManager;
    }

    // ==========================================================
    // DOCUMENT (PDF, EXCEL, MANUEL...)
    // ==========================================================
    public DocumentUploadResponse uploadDocument(MultipartFile file, String typeSource, Integer idAdmin) {
        String chemin = fileStorageService.store(file, "documents");

        DocumentSource doc = new DocumentSource();
        doc.setNomFichier(file.getOriginalFilename());
        doc.setTypeSource(typeSource);
        doc.setCheminFichier(chemin);
        doc.setDateImport(LocalDateTime.now());
        doc.setStatutIndexation("EN_ATTENTE");

        // uploader est optionnel (nullable) -> on le rattache si l'admin existe
        if (idAdmin != null) {
            Administrateur admin = administrateurRepository.findById(idAdmin)
                    .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable."));
            doc.setUploader(admin);
        }

        DocumentSource saved = documentSourceRepository.save(doc);

        return new DocumentUploadResponse(
                saved.getIdSource(),
                saved.getNomFichier(),
                saved.getTypeSource(),
                saved.getCheminFichier(),
                saved.getStatutIndexation()
        );
    }

    // ==========================================================
    // RESSOURCE D'INSTALLATION (guide, checklist...)
    // etablissement est OBLIGATOIRE (nullable = false en DB)
    // ==========================================================
    public RessourceUploadResponse uploadRessource(MultipartFile file, String titre, String type,
                                                   Integer idEtablissement) {
        if (idEtablissement == null) {
            throw new IllegalArgumentException("L'établissement est obligatoire pour une ressource.");
        }

        Etablissement etablissement = etablissementRepository.findById(idEtablissement)
                .orElseThrow(() -> new EntityNotFoundException("Établissement introuvable."));

        String chemin = fileStorageService.store(file, "ressources");

        RessourceInstallation ressource = new RessourceInstallation();
        ressource.setTitre(titre);
        ressource.setType(type);
        ressource.setCheminFichier(chemin);
        ressource.setValideParAdmin(true); // uploadé par un admin -> validé d'office
        ressource.setDateAjout(LocalDateTime.now());
        ressource.setEtablissement(etablissement);

        RessourceInstallation saved = ressourceInstallationRepository.save(ressource);

        return new RessourceUploadResponse(
                saved.getIdRessource(),
                saved.getTitre(),
                saved.getType(),
                saved.getCheminFichier()
        );
    }

    // ==========================================================
    // VIDEO (métadonnées uniquement, fichier hébergé sur YouTube)
    // ajouteParAdmin est OBLIGATOIRE (nullable = false en DB)
    // ==========================================================
    public VideoMateriel createVideo(CreateVideoRequest request, Integer idAdmin) {
        Administrateur admin = administrateurRepository.findById(idAdmin)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable."));

        VideoMateriel video = new VideoMateriel();
        video.setTitre(request.getTitre());
        video.setDescription(request.getDescription());
        video.setUrlVideo(request.getUrlVideo());
        video.setUrlMiniature(request.getUrlMiniature());
        video.setFournisseur(request.getFournisseur() != null ? request.getFournisseur() : "YouTube");
        video.setDureeSecondes(request.getDureeSecondes());
        video.setAjouteParAdmin(admin);
        video.setDateAjout(LocalDateTime.now());

        // categorie / materiel sont optionnels -> getReference évite un aller-retour DB
        // (l'entité n'est chargée que si on accède à un champ ; si l'id n'existe pas,
        // ça lèvera une exception au moment du save())
        if (request.getIdCategorie() != null) {
            video.setCategorie(entityManager.getReference(CategorieMateriel.class, request.getIdCategorie()));
        }
        if (request.getIdMateriel() != null) {
            video.setMateriel(entityManager.getReference(Materiel.class, request.getIdMateriel()));
        }

        return videoMaterielRepository.save(video);
    }
}