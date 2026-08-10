package com.example.backend.service.admin;

import com.example.backend.dto.admin.intervention.*;
import com.example.backend.entity.*;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.repository.admin.*;
import com.example.backend.service.NotificationHelperService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InterventionService {

    private final InterventionRepository interventionRepository;
    private final MissionInstallationRepository missionRepository;
    private final TechnicienRepository technicienRepository;
    private final PhotoRepository photoRepository;
    private final AttestationRepository attestationRepository;

    private final SortieMaterielRepository sortieMaterielRepository;
    private final RetourMaterielRepository retourMaterielRepository;
    private final ChecklistEquipementRepository checklistEquipementRepository;
    private final ChecklistItemRepository checklistItemRepository;

    private final MissionInstallationService missionInstallationService;

    private final UtilisateurRepository utilisateurRepository;
    private final NotificationHelperService notificationHelperService;

    public InterventionService(
            InterventionRepository interventionRepository,
            MissionInstallationRepository missionRepository,
            TechnicienRepository technicienRepository,
            PhotoRepository photoRepository,
            AttestationRepository attestationRepository,
            SortieMaterielRepository sortieMaterielRepository,
            RetourMaterielRepository retourMaterielRepository,
            ChecklistEquipementRepository checklistEquipementRepository,
            ChecklistItemRepository checklistItemRepository,
            MissionInstallationService missionInstallationService,
            UtilisateurRepository utilisateurRepository,
            NotificationHelperService notificationHelperService) {

        this.interventionRepository = interventionRepository;
        this.missionRepository = missionRepository;
        this.technicienRepository = technicienRepository;
        this.photoRepository = photoRepository;
        this.attestationRepository = attestationRepository;
        this.sortieMaterielRepository = sortieMaterielRepository;
        this.retourMaterielRepository = retourMaterielRepository;
        this.checklistEquipementRepository = checklistEquipementRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.missionInstallationService = missionInstallationService;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationHelperService = notificationHelperService;
    }

    public List<InterventionResponse> getAll() {
        List<Intervention> interventions = interventionRepository.findAll();
        List<InterventionResponse> responses = new ArrayList<>();
        for (Intervention intervention : interventions) {
            responses.add(convertToResponse(intervention));
        }
        return responses;
    }

    public InterventionResponse getById(Integer id) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));
        return convertToResponse(intervention);
    }

    public Intervention getInterventionEntity(Integer id) {
        return interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));
    }

    public InterventionResponse create(CreateInterventionRequest request) {
        MissionInstallation mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new EntityNotFoundException("Mission introuvable."));

        Technicien technicien = technicienRepository.findById(request.getTechnicienId())
                .orElseThrow(() -> new EntityNotFoundException("Technicien introuvable."));

        Intervention intervention = new Intervention();
        intervention.setDateDebut(request.getDateDebut());
        intervention.setDateFin(request.getDateFin());
        intervention.setTauxAvancement(request.getTauxAvancement());
        intervention.setNumeroVisite(request.getNumeroVisite());
        intervention.setStatut(request.getStatut());
        intervention.setLocalisationGps(request.getLocalisationGps());
        intervention.setMission(mission);
        intervention.setTechnicien(technicien);

        Intervention saved = interventionRepository.save(intervention);

        missionInstallationService.recalculerStatut(mission.getIdMission());

        return convertToResponse(saved);
    }

    /**
     * @param idAuteur id de l'utilisateur connecté qui effectue la mise à jour,
     *                 utilisé comme expéditeur si une notification de clôture
     *                 est déclenchée. Peut être null.
     */
    public InterventionResponse update(Integer id, UpdateInterventionRequest request, Integer idAuteur) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));

        MissionInstallation mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new EntityNotFoundException("Mission introuvable."));

        Technicien technicien = technicienRepository.findById(request.getTechnicienId())
                .orElseThrow(() -> new EntityNotFoundException("Technicien introuvable."));

        String ancienStatut = intervention.getStatut();

        intervention.setDateDebut(request.getDateDebut());
        intervention.setDateFin(request.getDateFin());
        intervention.setTauxAvancement(request.getTauxAvancement());
        intervention.setNumeroVisite(request.getNumeroVisite());
        intervention.setStatut(request.getStatut());
        intervention.setLocalisationGps(request.getLocalisationGps());
        intervention.setMission(mission);
        intervention.setTechnicien(technicien);

        Intervention updated = interventionRepository.save(intervention);

        missionInstallationService.recalculerStatut(mission.getIdMission());

        boolean vientDePasserATerminee = !"Exécutée".equals(ancienStatut) && "Exécutée".equals(updated.getStatut());
        if (vientDePasserATerminee && idAuteur != null) {
            utilisateurRepository.findById(idAuteur).ifPresent(auteur -> {
                String refMission = updated.getMission() != null ? updated.getMission().getReference() : "N/A";
                String message = "L'intervention " + refMission + " est passée au statut Terminée. "
                        + "Le matériel sorti doit être régularisé au stock.";
                notificationHelperService.notifierTousLesAdmins(auteur, message, "INTERVENTION_TERMINEE");
            });
        }

        return convertToResponse(updated);
    }

    public void delete(Integer id) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));
        Integer idMission = intervention.getMission().getIdMission();

        interventionRepository.deleteById(id);

        missionInstallationService.recalculerStatut(idMission);
    }

    private InterventionResponse convertToResponse(Intervention intervention) {
        InterventionResponse response = new InterventionResponse();
        response.setId(intervention.getIdIntervention());
        response.setDateDebut(intervention.getDateDebut());
        response.setDateFin(intervention.getDateFin());
        response.setTauxAvancement(intervention.getTauxAvancement());
        response.setNumeroVisite(intervention.getNumeroVisite());
        response.setStatut(intervention.getStatut());
        response.setLocalisationGps(intervention.getLocalisationGps());

        if (intervention.getMission() != null) {
            response.setMissionId(intervention.getMission().getIdMission());
            response.setMissionReference(intervention.getMission().getReference());

            if (intervention.getMission().getEtablissement() != null) {
                response.setEtablissementDesignation(intervention.getMission().getEtablissement().getDesignation());
            }
        }

        if (intervention.getTechnicien() != null) {
            response.setTechnicienId(intervention.getTechnicien().getId());
            response.setTechnicienNom(
                    intervention.getTechnicien().getNom() + " " + intervention.getTechnicien().getPrenom()
            );
        }

        List<Photo> photos = photoRepository.findByIntervention(intervention);
        if (photos != null && !photos.isEmpty()) {
            List<PhotoDto> photoDtos = photos.stream().map(p -> {
                PhotoDto dto = new PhotoDto();
                dto.setId(p.getIdPhoto());
                dto.setCheminFichier(p.getCheminFichier());
                dto.setTypePhoto(p.getTypePhoto());
                return dto;
            }).collect(Collectors.toList());
            response.setPhotos(photoDtos);
        }

        Optional<Attestation> attestationOpt = attestationRepository.findByIntervention(intervention);
        if (attestationOpt.isPresent()) {
            Attestation attestation = attestationOpt.get();
            AttestationDto attDto = new AttestationDto();
            attDto.setId(attestation.getIdAttestation());
            attDto.setNomSignataire(attestation.getNomSignataire());
            attDto.setDateSignature(attestation.getDateSignature());
            attDto.setSignatureNumerique(attestation.getSignatureNumerique());
            attDto.setValide(attestation.getValide());
            response.setAttestation(attDto);
        }

        List<SortieMateriel> sorties = sortieMaterielRepository.findByIntervention(intervention);
        if (sorties != null && !sorties.isEmpty()) {
            List<SortieMaterielDto> sortieDtos = sorties.stream().map(s -> {
                SortieMaterielDto dto = new SortieMaterielDto();
                dto.setIdSortie(s.getIdSortie());
                dto.setDateSortie(s.getDateSortie());

                if (s.getDetails() != null && !s.getDetails().isEmpty()) {
                    DetailSortieMateriel detail = s.getDetails().get(0);
                    dto.setMaterielReference(detail.getMateriel().getReference());
                    dto.setQuantite(detail.getQuantite());
                }
                return dto;
            }).collect(Collectors.toList());
            response.setSortiesMateriel(sortieDtos);
        }

        List<RetourMateriel> retours = retourMaterielRepository.findByIntervention(intervention);
        if (retours != null && !retours.isEmpty()) {
            List<RetourMaterielDto> retourDtos = retours.stream().map(r -> {
                RetourMaterielDto dto = new RetourMaterielDto();
                dto.setIdRetour(r.getIdRetour());
                dto.setDateRetour(r.getDateRetour());
                dto.setMaterielReference(r.getMateriel().getReference());
                dto.setQuantite(r.getQuantite());
                dto.setEtatMateriel(r.getEtatMateriel());
                return dto;
            }).collect(Collectors.toList());
            response.setRetoursMateriel(retourDtos);
        }

        Optional<ChecklistEquipement> checklistOpt = checklistEquipementRepository.findByIntervention(intervention);
        if (checklistOpt.isPresent()) {
            ChecklistEquipement checklist = checklistOpt.get();
            List<ChecklistItem> items = checklistItemRepository.findByChecklist(checklist);

            if (items != null && !items.isEmpty()) {
                List<ChecklistItemDto> itemDtos = items.stream().map(item -> {
                    ChecklistItemDto dto = new ChecklistItemDto();
                    dto.setIdItem(item.getIdItem());
                    dto.setMaterielReference(item.getMateriel().getReference());
                    dto.setQuantite(item.getQuantite());
                    dto.setEtatConstate(item.getEtatConstate());
                    dto.setConforme(item.getConforme());
                    return dto;
                }).collect(Collectors.toList());
                response.setChecklistItems(itemDtos);
            }
        }

        return response;
    }

    public InterventionResponse forceCompleteByAdmin(Integer id) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));

        intervention.setStatut("Clôturée");
        intervention.setTauxAvancement(100.0);

        Intervention updated = interventionRepository.save(intervention);

        missionInstallationService.recalculerStatut(intervention.getMission().getIdMission());

        return convertToResponse(updated);
    }
}