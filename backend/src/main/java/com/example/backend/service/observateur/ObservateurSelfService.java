package com.example.backend.service.observateur;

import com.example.backend.dto.admin.permission.*;
import com.example.backend.dto.observateur.*;
import com.example.backend.entity.*;
import com.example.backend.repository.ObservateurRepositoryy;
import com.example.backend.repository.admin.ObservateurDocumentRepository;
import com.example.backend.repository.admin.ObservateurResourceAssigneeRepository;
import com.example.backend.repository.admin.ObservateurVideoAssigneeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import com.example.backend.dto.observateur.DocumentFileDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ObservateurSelfService {

    private final ObservateurRepositoryy observateurRepositoryy;
    private final ObservateurVideoAssigneeRepository videoAssigneeRepository;
    private final ObservateurResourceAssigneeRepository resourceAssigneeRepository;
    private final ObservateurDocumentRepository documentAssigneeRepository;

    public ObservateurSelfService(
            ObservateurRepositoryy observateurRepositoryy,
            ObservateurVideoAssigneeRepository videoAssigneeRepository,
            ObservateurResourceAssigneeRepository resourceAssigneeRepository,
            ObservateurDocumentRepository documentAssigneeRepository) {
        this.observateurRepositoryy = observateurRepositoryy;
        this.videoAssigneeRepository = videoAssigneeRepository;
        this.resourceAssigneeRepository = resourceAssigneeRepository;
        this.documentAssigneeRepository = documentAssigneeRepository;
    }

    // ⚠️ Hypothèse : Authentication#getName() = email. À adapter si besoin.
    private Observateur resolveObservateur(String email) {
        return observateurRepositoryy.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Observateur introuvable pour : " + email));
    }

    public List<VideoAssignmentResponse> getMesVideos(String email) {
        Observateur obs = resolveObservateur(email);
        return videoAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId())
                .stream().map(this::toVideoResponse).collect(Collectors.toList());
    }

    public List<ResourceAssignmentResponse> getMesRessources(String email) {
        Observateur obs = resolveObservateur(email);
        return resourceAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId())
                .stream().map(this::toResourceResponse).collect(Collectors.toList());
    }

    public List<DocumentAssignmentResponse> getMesDocuments(String email) {
        Observateur obs = resolveObservateur(email);
        return documentAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId())
                .stream().map(this::toDocumentResponse).collect(Collectors.toList());
    }

    public ObservateurDashboardSummaryDto getSummary(String email) {
        Observateur obs = resolveObservateur(email);
        List<ObservateurVideoAssignee> videos = videoAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId());
        List<ObservateurResourceAssignee> ressources = resourceAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId());
        List<ObservateurDocument> documents = documentAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId());

        LocalDateTime dernier = Stream.of(
                        videos.stream().map(ObservateurVideoAssignee::getDateAssignation),
                        ressources.stream().map(ObservateurResourceAssignee::getDateAssignation),
                        documents.stream().map(ObservateurDocument::getDateAssignation))
                .flatMap(s -> s)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new ObservateurDashboardSummaryDto(
                videos.size(),
                ressources.size(),
                documents.size(),
                videos.size() + ressources.size() + documents.size(),
                dernier);
    }

    public List<DistributionItemDto> getDistribution(String email) {
        Observateur obs = resolveObservateur(email);
        int videos = videoAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId()).size();
        int ressources = resourceAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId()).size();
        int documents = documentAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId()).size();

        return List.of(
                new DistributionItemDto("Vidéos", videos, "#f97316"),
                new DistributionItemDto("Ressources", ressources, "#22c55e"),
                new DistributionItemDto("Documents", documents, "#3b82f6")
        );
    }

    public List<TimelinePointDto> getTimeline(String email) {
        Observateur obs = resolveObservateur(email);

        List<LocalDateTime> dates = new ArrayList<>();
        dates.addAll(videoAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId())
                .stream().map(ObservateurVideoAssignee::getDateAssignation).toList());
        dates.addAll(resourceAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId())
                .stream().map(ObservateurResourceAssignee::getDateAssignation).toList());
        dates.addAll(documentAssigneeRepository.findByObservateur_IdAndActifTrue(obs.getId())
                .stream().map(ObservateurDocument::getDateAssignation).toList());

        WeekFields wf = WeekFields.ISO;
        Map<Integer, Long> parSemaine = dates.stream()
                .collect(Collectors.groupingBy(d -> d.get(wf.weekOfWeekBasedYear()), Collectors.counting()));

        List<TimelinePointDto> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 7; i >= 0; i--) {
            int numSemaine = now.minusWeeks(i).get(wf.weekOfWeekBasedYear());
            long total = parSemaine.getOrDefault(numSemaine, 0L);
            result.add(new TimelinePointDto("S-" + i, total));
        }
        return result;
    }

    // --- Mapping identique à AdminPermissionService ---
    // TODO refactor : extraire ces 3 méthodes dans un @Component PermissionMapper
    // partagé entre AdminPermissionService et ObservateurSelfService pour éviter la duplication.

    private VideoAssignmentResponse toVideoResponse(ObservateurVideoAssignee a) {
        VideoAssignmentResponse dto = new VideoAssignmentResponse();
        dto.setId(a.getId());
        dto.setObservateur(new com.example.backend.dto.admin.ObservateurSummaryDto(
                a.getObservateur().getId(), a.getObservateur().getNom(), a.getObservateur().getPrenom(),
                a.getObservateur().getEmail(), a.getObservateur().getTypeClient()));
        dto.setIdVideo(a.getVideo().getIdVideo());
        dto.setTitreVideo(a.getVideo().getTitre());
        dto.setDateAssignation(a.getDateAssignation());
        dto.setActif(a.getActif());
        dto.setAssigneParAdminNom(a.getAssigneParAdmin().getNom() + " " + a.getAssigneParAdmin().getPrenom());
        return dto;
    }

    private ResourceAssignmentResponse toResourceResponse(ObservateurResourceAssignee a) {
        ResourceAssignmentResponse dto = new ResourceAssignmentResponse();
        dto.setId(a.getId());
        dto.setObservateur(new com.example.backend.dto.admin.ObservateurSummaryDto(
                a.getObservateur().getId(), a.getObservateur().getNom(), a.getObservateur().getPrenom(),
                a.getObservateur().getEmail(), a.getObservateur().getTypeClient()));
        dto.setIdRessource(a.getRessource().getIdRessource());
        dto.setTitreRessource(a.getRessource().getTitre());
        dto.setTypeRessource(a.getRessource().getType());
        dto.setDateAssignation(a.getDateAssignation());
        dto.setActif(a.getActif());
        dto.setAssigneParAdminNom(a.getAssigneParAdmin().getNom() + " " + a.getAssigneParAdmin().getPrenom());
        return dto;
    }

    private DocumentAssignmentResponse toDocumentResponse(ObservateurDocument a) {
        DocumentAssignmentResponse dto = new DocumentAssignmentResponse();
        dto.setId(a.getId());
        dto.setObservateur(new com.example.backend.dto.admin.ObservateurSummaryDto(
                a.getObservateur().getId(), a.getObservateur().getNom(), a.getObservateur().getPrenom(),
                a.getObservateur().getEmail(), a.getObservateur().getTypeClient()));
        dto.setIdDocument(a.getDocument().getIdSource());
        dto.setNomFichier(a.getDocument().getNomFichier());
        dto.setTypeDocument(a.getDocument().getTypeSource());
        dto.setDateAssignation(a.getDateAssignation());
        dto.setActif(a.getActif());
        dto.setAssigneParAdminNom(a.getAssigneParAdmin().getNom() + " " + a.getAssigneParAdmin().getPrenom());
        return dto;
    }



// ... dans la classe :

    @Value("${app.storage.documents-path:ia/data}")
    private String documentsStoragePath;

    public DocumentFileDto getDocumentFileForViewing(String email, Integer idDocument) {
        Observateur obs = resolveObservateur(email);

        ObservateurDocument assignment = documentAssigneeRepository
                .findByObservateur_IdAndDocument_IdSourceAndActifTrue(obs.getId(), idDocument)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Document introuvable ou non partagé avec vous."));

        String nomFichier = assignment.getDocument().getNomFichier();

        Path basePath = Paths.get(documentsStoragePath).normalize().toAbsolutePath();
        Path filePath = basePath.resolve(nomFichier).normalize();

        // Sécurité : empêche de sortir du dossier autorisé (path traversal)
        if (!filePath.startsWith(basePath)) {
            throw new SecurityException("Chemin de fichier invalide.");
        }

        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new EntityNotFoundException("Fichier introuvable : " + nomFichier);
        }

        if (!resource.exists() || !resource.isReadable()) {
            throw new EntityNotFoundException("Fichier introuvable ou illisible : " + nomFichier);
        }

        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return new DocumentFileDto(resource, nomFichier, contentType);
    }
}