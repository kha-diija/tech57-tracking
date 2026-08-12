package com.example.backend.service.admin;

import com.example.backend.dto.admin.ObservateurSummaryDto;
import com.example.backend.dto.admin.permission.*;
import com.example.backend.entity.*;
import com.example.backend.repository.AdministrateurRepository;
import com.example.backend.repository.DocumentSourceRepository;
import com.example.backend.repository.ObservateurRepository;
import com.example.backend.repository.RessourceInstallationRepository;
import com.example.backend.repository.VideoMaterielRepository;
import com.example.backend.repository.admin.ObservateurDocumentRepository;
import com.example.backend.repository.admin.ObservateurResourceAssigneeRepository;
import com.example.backend.repository.admin.ObservateurVideoAssigneeRepository;
import jakarta.persistence.EntityNotFoundException;
import com.example.backend.dto.admin.permission.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminPermissionService {

    private final ObservateurRepository observateurRepository;
    private final AdministrateurRepository administrateurRepository;

    private final VideoMaterielRepository videoMaterielRepository;
    private final RessourceInstallationRepository ressourceInstallationRepository;
    private final DocumentSourceRepository documentSourceRepository;

    private final ObservateurVideoAssigneeRepository videoAssigneeRepository;
    private final ObservateurResourceAssigneeRepository resourceAssigneeRepository;
    private final ObservateurDocumentRepository documentAssigneeRepository;

    public AdminPermissionService(
            ObservateurRepository observateurRepository,
            AdministrateurRepository administrateurRepository,
            VideoMaterielRepository videoMaterielRepository,
            RessourceInstallationRepository ressourceInstallationRepository,
            DocumentSourceRepository documentSourceRepository,
            ObservateurVideoAssigneeRepository videoAssigneeRepository,
            ObservateurResourceAssigneeRepository resourceAssigneeRepository,
            ObservateurDocumentRepository documentAssigneeRepository) {
        this.observateurRepository = observateurRepository;
        this.administrateurRepository = administrateurRepository;
        this.videoMaterielRepository = videoMaterielRepository;
        this.ressourceInstallationRepository = ressourceInstallationRepository;
        this.documentSourceRepository = documentSourceRepository;
        this.videoAssigneeRepository = videoAssigneeRepository;
        this.resourceAssigneeRepository = resourceAssigneeRepository;
        this.documentAssigneeRepository = documentAssigneeRepository;
    }

    // ==========================================================
    // LISTE DES OBSERVATEURS (pour le dropdown côté admin)
    // ==========================================================
    public List<ObservateurSummaryDto> getAllObservateurs() {
        return observateurRepository.findAll().stream()
                .map(o -> new ObservateurSummaryDto(
                        o.getId(),
                        o.getNom(),
                        o.getPrenom(),
                        o.getEmail(),
                        o.getTypeClient()))
                .collect(Collectors.toList());
    }

    // ==========================================================
    // VIDEOS
    // ==========================================================
    public VideoAssignmentResponse assignVideo(CreateVideoAssignmentRequest request, Integer idAdmin) {
        Observateur observateur = observateurRepository.findById(request.getIdObservateur())
                .orElseThrow(() -> new EntityNotFoundException("Observateur introuvable."));
        VideoMateriel video = videoMaterielRepository.findById(request.getIdVideo())
                .orElseThrow(() -> new EntityNotFoundException("Vidéo introuvable."));
        Administrateur admin = administrateurRepository.findById(idAdmin)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable."));

        ObservateurVideoAssignee assignation = videoAssigneeRepository
                .findByObservateur_IdAndVideo_IdVideo(observateur.getId(), video.getIdVideo())
                .orElseGet(ObservateurVideoAssignee::new);

        assignation.setObservateur(observateur);
        assignation.setVideo(video);
        assignation.setAssigneParAdmin(admin);
        assignation.setActif(true);
        assignation.setDateAssignation(LocalDateTime.now());

        ObservateurVideoAssignee saved = videoAssigneeRepository.save(assignation);
        return toVideoResponse(saved);
    }

    public void revokeVideo(Integer idObservateur, Integer idVideo) {
        ObservateurVideoAssignee assignation = videoAssigneeRepository
                .findByObservateur_IdAndVideo_IdVideo(idObservateur, idVideo)
                .orElseThrow(() -> new EntityNotFoundException("Assignation introuvable."));
        assignation.setActif(false);
        videoAssigneeRepository.save(assignation);
    }

    public List<VideoAssignmentResponse> getVideosAssignedTo(Integer idObservateur) {
        return videoAssigneeRepository.findByObservateur_IdAndActifTrue(idObservateur)
                .stream().map(this::toVideoResponse).collect(Collectors.toList());
    }

    private VideoAssignmentResponse toVideoResponse(ObservateurVideoAssignee a) {
        VideoAssignmentResponse dto = new VideoAssignmentResponse();
        dto.setId(a.getId());
        dto.setObservateur(new ObservateurSummaryDto(
                a.getObservateur().getId(),
                a.getObservateur().getNom(),
                a.getObservateur().getPrenom(),
                a.getObservateur().getEmail(),
                a.getObservateur().getTypeClient()));
        dto.setIdVideo(a.getVideo().getIdVideo());
        dto.setTitreVideo(a.getVideo().getTitre());
        dto.setDateAssignation(a.getDateAssignation());
        dto.setActif(a.getActif());
        dto.setAssigneParAdminNom(a.getAssigneParAdmin().getNom() + " " + a.getAssigneParAdmin().getPrenom());
        return dto;
    }

    // ==========================================================
    // RESSOURCES (guides, manuels...)
    // ==========================================================
    public ResourceAssignmentResponse assignResource(CreateResourceAssignmentRequest request, Integer idAdmin) {
        Observateur observateur = observateurRepository.findById(request.getIdObservateur())
                .orElseThrow(() -> new EntityNotFoundException("Observateur introuvable."));
        RessourceInstallation ressource = ressourceInstallationRepository.findById(request.getIdRessource())
                .orElseThrow(() -> new EntityNotFoundException("Ressource introuvable."));
        Administrateur admin = administrateurRepository.findById(idAdmin)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable."));

        ObservateurResourceAssignee assignation = resourceAssigneeRepository
                .findByObservateur_IdAndRessource_IdRessource(observateur.getId(), ressource.getIdRessource())
                .orElseGet(ObservateurResourceAssignee::new);

        assignation.setObservateur(observateur);
        assignation.setRessource(ressource);
        assignation.setAssigneParAdmin(admin);
        assignation.setActif(true);
        assignation.setDateAssignation(LocalDateTime.now());

        ObservateurResourceAssignee saved = resourceAssigneeRepository.save(assignation);
        return toResourceResponse(saved);
    }

    public void revokeResource(Integer idObservateur, Integer idRessource) {
        ObservateurResourceAssignee assignation = resourceAssigneeRepository
                .findByObservateur_IdAndRessource_IdRessource(idObservateur, idRessource)
                .orElseThrow(() -> new EntityNotFoundException("Assignation introuvable."));
        assignation.setActif(false);
        resourceAssigneeRepository.save(assignation);
    }

    public List<ResourceAssignmentResponse> getResourcesAssignedTo(Integer idObservateur) {
        return resourceAssigneeRepository.findByObservateur_IdAndActifTrue(idObservateur)
                .stream().map(this::toResourceResponse).collect(Collectors.toList());
    }

    private ResourceAssignmentResponse toResourceResponse(ObservateurResourceAssignee a) {
        ResourceAssignmentResponse dto = new ResourceAssignmentResponse();
        dto.setId(a.getId());
        dto.setObservateur(new ObservateurSummaryDto(
                a.getObservateur().getId(),
                a.getObservateur().getNom(),
                a.getObservateur().getPrenom(),
                a.getObservateur().getEmail(),
                a.getObservateur().getTypeClient()));
        dto.setIdRessource(a.getRessource().getIdRessource());
        dto.setTitreRessource(a.getRessource().getTitre());
        dto.setTypeRessource(a.getRessource().getType());
        dto.setDateAssignation(a.getDateAssignation());
        dto.setActif(a.getActif());
        dto.setAssigneParAdminNom(a.getAssigneParAdmin().getNom() + " " + a.getAssigneParAdmin().getPrenom());
        return dto;
    }

    // ==========================================================
    // DOCUMENTS (PDF)
    // ==========================================================
    public DocumentAssignmentResponse assignDocument(CreateDocumentAssignmentRequest request, Integer idAdmin) {
        Observateur observateur = observateurRepository.findById(request.getIdObservateur())
                .orElseThrow(() -> new EntityNotFoundException("Observateur introuvable."));
        DocumentSource document = documentSourceRepository.findById(request.getIdDocument())
                .orElseThrow(() -> new EntityNotFoundException("Document introuvable."));
        Administrateur admin = administrateurRepository.findById(idAdmin)
                .orElseThrow(() -> new EntityNotFoundException("Administrateur introuvable."));

        ObservateurDocument assignation = documentAssigneeRepository
                .findByObservateur_IdAndDocument_IdSource(observateur.getId(), document.getIdSource())
                .orElseGet(ObservateurDocument::new);

        assignation.setObservateur(observateur);
        assignation.setDocument(document);
        assignation.setAssigneParAdmin(admin);
        assignation.setActif(true);
        assignation.setDateAssignation(LocalDateTime.now());

        ObservateurDocument saved = documentAssigneeRepository.save(assignation);
        return toDocumentResponse(saved);
    }

    public void revokeDocument(Integer idObservateur, Integer idDocument) {
        ObservateurDocument assignation = documentAssigneeRepository
                .findByObservateur_IdAndDocument_IdSource(idObservateur, idDocument)
                .orElseThrow(() -> new EntityNotFoundException("Assignation introuvable."));
        assignation.setActif(false);
        documentAssigneeRepository.save(assignation);
    }

    public List<DocumentAssignmentResponse> getDocumentsAssignedTo(Integer idObservateur) {
        return documentAssigneeRepository.findByObservateur_IdAndActifTrue(idObservateur)
                .stream().map(this::toDocumentResponse).collect(Collectors.toList());
    }

    private DocumentAssignmentResponse toDocumentResponse(ObservateurDocument a) {
        DocumentAssignmentResponse dto = new DocumentAssignmentResponse();
        dto.setId(a.getId());
        dto.setObservateur(new ObservateurSummaryDto(
                a.getObservateur().getId(),
                a.getObservateur().getNom(),
                a.getObservateur().getPrenom(),
                a.getObservateur().getEmail(),
                a.getObservateur().getTypeClient()));
        dto.setIdDocument(a.getDocument().getIdSource());
        dto.setNomFichier(a.getDocument().getNomFichier());
        dto.setTypeDocument(a.getDocument().getTypeSource());
        dto.setDateAssignation(a.getDateAssignation());
        dto.setActif(a.getActif());
        dto.setAssigneParAdminNom(a.getAssigneParAdmin().getNom() + " " + a.getAssigneParAdmin().getPrenom());
        return dto;
    }
    // ==========================================================
    // CATALOGUES (pour les listes déroulantes du modal d'assignation)
    // ==========================================================
    public List<VideoCatalogDto> getAllVideosCatalog() {
        return videoMaterielRepository.findAll().stream()
                .map(v -> new VideoCatalogDto(v.getIdVideo(), v.getTitre(), v.getFournisseur(), v.getDureeSecondes()))
                .collect(Collectors.toList());
    }

    public List<RessourceCatalogDto> getAllRessourcesCatalog() {
        return ressourceInstallationRepository.findAll().stream()
                .map(r -> new RessourceCatalogDto(r.getIdRessource(), r.getTitre(), r.getType()))
                .collect(Collectors.toList());
    }

    public List<DocumentCatalogDto> getAllDocumentsCatalog() {
        return documentSourceRepository.findAll().stream()
                .map(d -> new DocumentCatalogDto(d.getIdSource(), d.getNomFichier(), d.getTypeSource()))
                .collect(Collectors.toList());
    }
}