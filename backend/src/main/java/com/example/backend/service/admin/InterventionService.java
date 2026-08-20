package com.example.backend.service.admin;

import com.example.backend.dto.admin.intervention.*;
import com.example.backend.entity.*;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.repository.admin.*;
import com.example.backend.service.NotificationHelperService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        intervention.setDatePrevue(request.getDatePrevue());
        intervention.setDateDebut(request.getDateDebut());
        intervention.setDateFin(request.getDateFin());
        intervention.setTauxAvancement(request.getTauxAvancement() != null ? request.getTauxAvancement() : 0.0);
        intervention.setNumeroVisite(request.getNumeroVisite() != null ? request.getNumeroVisite() : 0);
        intervention.setStatut(request.getStatut() != null ? request.getStatut() : "Planifiée");
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

        intervention.setDatePrevue(request.getDatePrevue());
        intervention.setDateDebut(request.getDateDebut());
        intervention.setDateFin(request.getDateFin());
        intervention.setTauxAvancement(request.getTauxAvancement());
        intervention.setLocalisationGps(request.getLocalisationGps());
        intervention.setMission(mission);
        intervention.setTechnicien(technicien);

        // --- VALIDATION MÉTIER : basée sur les vraies visites (checkInOuts) ET l'avancement ---
        List<CheckInOut> visitesActuelles = intervention.getCheckInOuts() != null
                ? intervention.getCheckInOuts() : new ArrayList<>();

        long visitesTermineesCount = visitesActuelles.stream()
                .filter(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() != null)
                .count();
        boolean visiteEnCoursExiste = visitesActuelles.stream()
                .anyMatch(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() == null);

        double avancementDemande = request.getTauxAvancement() != null ? request.getTauxAvancement() : 0.0;
        boolean travailCommence = !visitesActuelles.isEmpty() || avancementDemande > 0;

        String statutDemande = request.getStatut();

        if ("Exécutée".equals(statutDemande) || "Clôturée".equals(statutDemande)) {
            if (visitesTermineesCount < 2) {
                throw new IllegalArgumentException(
                        "Impossible de clôturer : l'intervention doit comporter au moins 2 visites terminées (check-in + check-out).");
            }
            if (visiteEnCoursExiste) {
                throw new IllegalArgumentException(
                        "Impossible de clôturer : une visite en cours n'a pas encore de check-out enregistré.");
            }
            // --- NOUVEAU : la clôture exige en plus un avancement à 100% ---
            if ("Clôturée".equals(statutDemande) && avancementDemande < 100.0) {
                throw new IllegalArgumentException(
                        "Impossible de clôturer : l'avancement doit être à 100% (actuellement " + avancementDemande + "%).");
            }
        } else if (travailCommence && "Planifiée".equals(statutDemande)) {
            statutDemande = "En cours";
        }

        intervention.setStatut(statutDemande);
        intervention.setNumeroVisite(visitesActuelles.size());

        Intervention updated = interventionRepository.save(intervention);

        missionInstallationService.recalculerStatut(mission.getIdMission());

        boolean vientDePasserATerminee = !"Exécutée".equals(ancienStatut) && "Exécutée".equals(updated.getStatut());
        if (vientDePasserATerminee && idAuteur != null) {
            utilisateurRepository.findById(idAuteur).ifPresent(auteur -> {
                String refMission = updated.getMission() != null ? updated.getMission().getReference() : "N/A";
                String message = "L'intervention " + refMission + " est passée au statut Exécutée. "
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

    public InterventionResponse convertToResponse(Intervention intervention) {
        InterventionResponse response = new InterventionResponse();
        response.setId(intervention.getIdIntervention());
        response.setDatePrevue(intervention.getDatePrevue());
        response.setDateDebut(intervention.getDateDebut());
        response.setDateFin(intervention.getDateFin());
        response.setLocalisationGps(intervention.getLocalisationGps());

        // --- CALCUL DU STATUT : basé sur les vraies visites (checkInOuts) ET l'avancement ---
        List<CheckInOut> visites = intervention.getCheckInOuts() != null
                ? intervention.getCheckInOuts() : new ArrayList<>();

        long visitesTerminees = visites.stream()
                .filter(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() != null)
                .count();
        boolean uneVisiteEnCours = visites.stream()
                .anyMatch(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() == null);

        double avancement = intervention.getTauxAvancement() != null ? intervention.getTauxAvancement() : 0.0;
        boolean travailCommence = !visites.isEmpty() || avancement > 0;

        String statutAffichage;

        if (!travailCommence) {
            if (intervention.getDatePrevue() != null
                    && intervention.getDatePrevue().isBefore(LocalDateTime.now())) {
                statutAffichage = "En retard";
            } else {
                statutAffichage = "Planifiée";
            }
        } else if (visitesTerminees >= 2 && !uneVisiteEnCours) {
            statutAffichage = "Exécutée";
        } else {
            statutAffichage = "En cours";
        }

        if ("Clôturée".equals(intervention.getStatut())) {
            statutAffichage = "Clôturée";
        }

        // --- CORRECTION : l'avancement ne doit jamais rester à 0/null si des visites existent ---
        // On ne force JAMAIS 100% automatiquement sur "Exécutée" : ça reste un choix
        // explicite de l'admin/technicien, la clôture réelle exigeant elle 100%.
        double avancementAffiche = avancement;
        if (!visites.isEmpty() && avancementAffiche <= 0.0) {
            avancementAffiche = Math.min(95.0, visitesTerminees * 40.0);
        }

        response.setStatut(statutAffichage);
        response.setTauxAvancement(avancementAffiche);
        response.setNumeroVisite(visites.size());

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

        if (!visites.isEmpty()) {
            List<CheckInOutDto> checkInOutDtos = visites.stream().map(cio -> {
                CheckInOutDto dto = new CheckInOutDto();
                dto.setIdCheckinout(cio.getIdCheckinout());
                dto.setNumeroVisite(cio.getNumeroVisite());
                dto.setDateHeureCheckin(cio.getDateHeureCheckin());
                dto.setDateHeureCheckout(cio.getDateHeureCheckout());
                dto.setDureeMinutes(cio.getDureeMinutes());
                dto.setGpsCheckin(cio.getGpsCheckin());
                dto.setGpsCheckout(cio.getGpsCheckout());
                return dto;
            }).collect(Collectors.toList());
            response.setCheckInOuts(checkInOutDtos);
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

        List<CheckInOut> visites = intervention.getCheckInOuts() != null
                ? intervention.getCheckInOuts() : new ArrayList<>();

        long visitesTerminees = visites.stream()
                .filter(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() != null)
                .count();
        boolean uneVisiteEnCours = visites.stream()
                .anyMatch(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() == null);
        double avancement = intervention.getTauxAvancement() != null ? intervention.getTauxAvancement() : 0.0;

        if (visitesTerminees < 2) {
            throw new IllegalArgumentException(
                    "Impossible de clôturer : au moins 2 visites terminées (check-in + check-out) sont requises.");
        }
        if (uneVisiteEnCours) {
            throw new IllegalArgumentException(
                    "Impossible de clôturer : une visite est encore en cours (pas de check-out).");
        }
        // --- NOUVEAU : la clôture exige en plus un avancement à 100% ---
        if (avancement < 100.0) {
            throw new IllegalArgumentException(
                    "Impossible de clôturer : l'avancement doit être à 100% (actuellement " + avancement + "%).");
        }

        intervention.setStatut("Clôturée");
        intervention.setTauxAvancement(100.0);

        Intervention updated = interventionRepository.save(intervention);

        missionInstallationService.recalculerStatut(intervention.getMission().getIdMission());

        return convertToResponse(updated);
    }
}