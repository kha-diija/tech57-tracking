package com.example.backend.service.technicien;

import com.example.backend.dto.admin.intervention.*;
import com.example.backend.dto.technicien.Dashboard.CheckInRequest;
import com.example.backend.dto.technicien.Dashboard.CheckOutRequest;
import com.example.backend.dto.technicien.Dashboard.MissionSimplifieeDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.admin.*;
import com.example.backend.service.admin.InterventionService;
import com.example.backend.service.admin.RapportPdfService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TechnicienInterventionService {

    private final InterventionRepository interventionRepository;
    private final CheckInOutRepository checkInOutRepository;
    private final PhotoRepository photoRepository;
    private final SortieMaterielRepository sortieMaterielRepository;
    private final RetourMaterielRepository retourMaterielRepository;
    private final MissionInstallationRepository missionRepository;
    private final TechnicienRepository technicienRepository;
    private final EquipeTechniqueRepository equipeTechniqueRepository;
    private final ChecklistEquipementRepository checklistEquipementRepository;
    private final ChecklistItemRepository checklistItemRepository;
    private final AttestationRepository attestationRepository;
    private final MaterielRepository materielRepository;

    private final InterventionService adminInterventionService;
    private final RapportPdfService rapportPdfService;

    public TechnicienInterventionService(InterventionRepository interventionRepository,
                                         CheckInOutRepository checkInOutRepository,
                                         PhotoRepository photoRepository,
                                         SortieMaterielRepository sortieMaterielRepository,
                                         RetourMaterielRepository retourMaterielRepository,
                                         MissionInstallationRepository missionRepository,
                                         TechnicienRepository technicienRepository,
                                         EquipeTechniqueRepository equipeTechniqueRepository,
                                         ChecklistEquipementRepository checklistEquipementRepository,
                                         ChecklistItemRepository checklistItemRepository,
                                         AttestationRepository attestationRepository,
                                         MaterielRepository materielRepository,
                                         InterventionService adminInterventionService,
                                         RapportPdfService rapportPdfService) {
        this.interventionRepository = interventionRepository;
        this.checkInOutRepository = checkInOutRepository;
        this.photoRepository = photoRepository;
        this.sortieMaterielRepository = sortieMaterielRepository;
        this.retourMaterielRepository = retourMaterielRepository;
        this.missionRepository = missionRepository;
        this.technicienRepository = technicienRepository;
        this.equipeTechniqueRepository = equipeTechniqueRepository;
        this.checklistEquipementRepository = checklistEquipementRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.attestationRepository = attestationRepository;
        this.materielRepository = materielRepository;
        this.adminInterventionService = adminInterventionService;
        this.rapportPdfService = rapportPdfService;
    }

    public List<InterventionResponse> getMesInterventions(Integer technicienId) {
        List<Intervention> interventions = interventionRepository.findByTechnicienId(technicienId);
        return interventions.stream()
                .map(adminInterventionService::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MissionSimplifieeDTO> getMissionsForTechnicien(Integer technicienId) {
        EquipeTechnique equipe = equipeTechniqueRepository.findByMembreId(technicienId).orElse(null);
        if (equipe == null) return new ArrayList<>();
        List<MissionInstallation> missions = missionRepository.findByEquipeIdEquipe(equipe.getIdEquipe());
        List<MissionSimplifieeDTO> result = new ArrayList<>();
        for (MissionInstallation m : missions) {
            MissionSimplifieeDTO dto = new MissionSimplifieeDTO();
            dto.setIdMission(m.getIdMission());
            dto.setReference(m.getReference());
            dto.setTitre(m.getTitre());
            dto.setStatut(m.getStatut());
            dto.setDateCreation(m.getDateCreation());
            dto.setBudgetPropose(m.getBudgetPropose());
            if (m.getEtablissement() != null) {
                dto.setEtablissementId(m.getEtablissement().getIdEtablissement());
                dto.setEtablissementDesignation(m.getEtablissement().getDesignation());
            }
            result.add(dto);
        }
        return result;
    }

    public InterventionResponse getInterventionById(Integer id, Integer technicienId) {
        Intervention intervention = interventionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));
        if (!intervention.getTechnicien().getId().equals(technicienId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à consulter cette intervention.");
        }
        return adminInterventionService.convertToResponse(intervention);
    }

    @Transactional
    public InterventionResponse createIntervention(Integer technicienId, CreateInterventionRequest request) {
        MissionInstallation mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new EntityNotFoundException("Mission introuvable."));
        Technicien technicien = technicienRepository.findById(technicienId)
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
        return adminInterventionService.convertToResponse(saved);
    }

    public byte[] genererRapport(Integer interventionId) throws Exception {
        return rapportPdfService.genererRapportPdf(interventionId);
    }

    public byte[] genererAttestation(Integer interventionId) throws Exception {
        return rapportPdfService.genererAttestationPdf(interventionId);
    }

    @Transactional
    public InterventionResponse updateIntervention(Integer id, Integer technicienId, UpdateInterventionRequest request) {
        Intervention intervention = checkTechnicienAccess(id, technicienId);
        MissionInstallation mission = missionRepository.findById(request.getMissionId())
                .orElseThrow(() -> new EntityNotFoundException("Mission introuvable."));
        intervention.setDatePrevue(request.getDatePrevue());
        intervention.setDateDebut(request.getDateDebut());
        intervention.setDateFin(request.getDateFin());
        intervention.setTauxAvancement(request.getTauxAvancement());
        intervention.setLocalisationGps(request.getLocalisationGps());
        intervention.setMission(mission);
        intervention.setStatut(request.getStatut());
        Intervention updated = interventionRepository.save(intervention);
        return adminInterventionService.convertToResponse(updated);
    }

    @Transactional
    public InterventionResponse checkIn(Integer interventionId, Integer technicienId, CheckInRequest request) {
        Intervention intervention = checkTechnicienAccess(interventionId, technicienId);
        CheckInOut currentVisit = checkInOutRepository
                .findByInterventionIdInterventionAndDateHeureCheckoutIsNull(interventionId)
                .orElse(new CheckInOut());
        if (currentVisit.getIdCheckinout() == null) {
            currentVisit.setIntervention(intervention);
            currentVisit.setNumeroVisite(intervention.getCheckInOuts().size() + 1);
        }
        currentVisit.setDateHeureCheckin(LocalDateTime.now());
        currentVisit.setGpsCheckin(request.getGpsCheckin());
        checkInOutRepository.save(currentVisit);
        updateInterventionStatus(intervention);
        return adminInterventionService.convertToResponse(intervention);
    }

    @Transactional
    public InterventionResponse checkOut(Integer interventionId, Integer technicienId, CheckOutRequest request, String checklistJson) throws IOException {
        Intervention intervention = checkTechnicienAccess(interventionId, technicienId);
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new EntityNotFoundException("Technicien introuvable."));

        CheckInOut currentVisit = checkInOutRepository
                .findByInterventionIdInterventionAndDateHeureCheckoutIsNull(interventionId)
                .orElseThrow(() -> new IllegalStateException("Aucune visite en cours pour cette intervention."));
        currentVisit.setDateHeureCheckout(LocalDateTime.now());
        currentVisit.setGpsCheckout(request.getGpsCheckout());
        long minutes = java.time.Duration.between(currentVisit.getDateHeureCheckin(), currentVisit.getDateHeureCheckout()).toMinutes();
        currentVisit.setDureeMinutes((int) minutes);
        checkInOutRepository.save(currentVisit);

        if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
            for (int i = 0; i < request.getPhotos().size(); i++) {
                String type = (request.getPhotoTypes() != null && i < request.getPhotoTypes().size()) ? request.getPhotoTypes().get(i) : "Photo";
                Photo photo = new Photo();
                photo.setCheminFichier("https://images.unsplash.com/photo-1581092160607-ee22621dd758?w=500");
                photo.setTypePhoto(type);
                photo.setIntervention(intervention);
                photoRepository.save(photo);
            }
        }

        if (request.getAttestationFile() != null && !request.getAttestationFile().isEmpty()) {
            MultipartFile attFile = request.getAttestationFile();
            String base64Signature = "data:" + attFile.getContentType() + ";base64," + Base64.getEncoder().encodeToString(attFile.getBytes());

            List<Attestation> existing = attestationRepository.findByIntervention(intervention);
            Attestation attestation;
            if (!existing.isEmpty()) {
                attestation = existing.get(0);
                // supprime les doublons résiduels s'il y en a
                for (int i = 1; i < existing.size(); i++) {
                    attestationRepository.delete(existing.get(i));
                }
            } else {
                attestation = new Attestation();
            }
            attestation.setIntervention(intervention);
            attestation.setNomSignataire(request.getSignataire() != null ? request.getSignataire() : technicien.getNom() + " " + technicien.getPrenom());
            attestation.setDateSignature(LocalDateTime.now());
            attestation.setValide(true);
            attestation.setSignatureNumerique(base64Signature);
            attestationRepository.save(attestation);
        }

        if (request.getMaterielRetourIds() != null && !request.getMaterielRetourIds().isEmpty()) {
            List<Integer> ids = request.getMaterielRetourIds();
            List<String> etats = request.getEtatsRetours();
            for (int i = 0; i < ids.size(); i++) {
                Integer idMateriel = ids.get(i);
                if (idMateriel == null) continue;
                String etat = (etats != null && i < etats.size()) ? etats.get(i) : "Bon état";
                Materiel materiel = materielRepository.findById(idMateriel).orElse(null);
                if (materiel == null) continue;
                RetourMateriel retour = new RetourMateriel();
                retour.setDateRetour(LocalDateTime.now());
                retour.setQuantite(1);
                retour.setEtatMateriel(etat);
                retour.setMateriel(materiel);
                retour.setTechnicien(technicien);
                retour.setIntervention(intervention);
                retourMaterielRepository.save(retour);
            }
        }

        // --- Checklist : reçue en JSON texte (multipart), parsée manuellement ---
        if (checklistJson != null && !checklistJson.isBlank()) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<ChecklistItemDto> checklistItems = mapper.readValue(
                    checklistJson,
                    new com.fasterxml.jackson.core.type.TypeReference<List<ChecklistItemDto>>() {});

            // Réutilise la checklist existante s'il y en a une, plutôt que d'en créer une nouvelle à chaque check-out
            List<ChecklistEquipement> existingChecklists = checklistEquipementRepository.findByIntervention(intervention);
            ChecklistEquipement checklistEquipement;
            if (!existingChecklists.isEmpty()) {
                checklistEquipement = existingChecklists.get(0);
                // supprime les doublons résiduels s'il y en a
                for (int i = 1; i < existingChecklists.size(); i++) {
                    checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(existingChecklists.get(i)));
                    checklistEquipementRepository.delete(existingChecklists.get(i));
                }
                // on repart d'une checklist propre : on supprime ses anciens items avant de reconstruire
                checklistItemRepository.deleteAll(checklistItemRepository.findByChecklist(checklistEquipement));
            } else {
                checklistEquipement = new ChecklistEquipement();
                checklistEquipement.setIntervention(intervention);
            }
            checklistEquipement.setTypeChecklist("Installé");
            checklistEquipement.setDateValidation(LocalDateTime.now());
            checklistEquipement = checklistEquipementRepository.save(checklistEquipement);

            for (ChecklistItemDto dto : checklistItems) {
                ChecklistItem item = new ChecklistItem();
                item.setChecklist(checklistEquipement);
                item.setQuantite(dto.getQuantite() != null ? dto.getQuantite() : 1);
                item.setEtatConstate(dto.getEtatConstate() != null ? dto.getEtatConstate() : "Bon état");
                item.setConforme(dto.getConforme() != null ? dto.getConforme() : true);

                Materiel materiel = null;
                if (dto.getIdMateriel() != null) {
                    materiel = materielRepository.findById(dto.getIdMateriel()).orElse(null);
                }
                if (materiel == null && dto.getMaterielReference() != null && !dto.getMaterielReference().isBlank()) {
                    materiel = materielRepository.findByReference(dto.getMaterielReference()).orElse(null);
                }
                if (materiel == null) {
                    materiel = materielRepository.findAll().stream().findFirst()
                            .orElseThrow(() -> new IllegalStateException("Aucun matériel disponible en base pour associer à l'élément de checklist."));
                }

                item.setMateriel(materiel);
                checklistItemRepository.save(item);
            }
        }

        updateInterventionStatus(intervention);
        return adminInterventionService.convertToResponse(intervention);
    }

    private Intervention checkTechnicienAccess(Integer interventionId, Integer technicienId) {
        Intervention intervention = interventionRepository.findById(interventionId)
                .orElseThrow(() -> new EntityNotFoundException("Intervention introuvable."));
        if (!intervention.getTechnicien().getId().equals(technicienId)) {
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette intervention.");
        }
        return intervention;
    }

    private void updateInterventionStatus(Intervention intervention) {
        List<CheckInOut> visites = intervention.getCheckInOuts();

        boolean visiteEnCours = visites.stream()
                .anyMatch(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() == null);

        long visitesTerminees = visites.stream()
                .filter(v -> v.getDateHeureCheckout() != null)
                .count();

        intervention.setNumeroVisite((int) visitesTerminees);

        if (visiteEnCours) {
            intervention.setStatut("En cours");
        } else if (visitesTerminees >= 2) {
            intervention.setStatut("Exécutée");
        } else if (visitesTerminees >= 1) {
            intervention.setStatut("Planifiée");
        }
        // Si aucune visite n'a encore eu lieu, on ne touche pas au statut existant

        interventionRepository.save(intervention);
    }
}