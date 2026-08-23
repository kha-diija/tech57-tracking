package com.example.backend.service.admin;

import com.example.backend.dto.admin.etablissement.*;
import com.example.backend.entity.Commune;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.Responsable;
import com.example.backend.repository.admin.*;
import com.example.backend.entity.Intervention;
import com.example.backend.entity.MissionInstallation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class EtablissementService {

    private final EtablissementRepository etablissementRepository;
    private final CommuneRepository communeRepository;
    private final ResponsableRepository responsableRepository;
    private final MissionInstallationRepository missionInstallationRepository;
    private final InterventionRepository interventionRepository;
    private final RapportRepository rapportRepository;
    private final PhotoRepository photoRepository;
    private final AttestationRepository attestationRepository;
    private final ChecklistEquipementRepository checklistEquipementRepository;
    private final ChecklistItemRepository checklistItemRepository;// <-- Ajout de la dépendance

    private final ObservateurRepository observateurRepository;

    public EtablissementService(EtablissementRepository etablissementRepository,
                                CommuneRepository communeRepository,
                                ResponsableRepository responsableRepository,
                                MissionInstallationRepository missionInstallationRepository,
                                InterventionRepository interventionRepository,
                                RapportRepository rapportRepository,
                                PhotoRepository photoRepository,
                                AttestationRepository attestationRepository,
                                ChecklistEquipementRepository checklistEquipementRepository,
                                ChecklistItemRepository checklistItemRepository,
                                ObservateurRepository observateurRepository) {
        this.etablissementRepository = etablissementRepository;
        this.communeRepository = communeRepository;
        this.responsableRepository = responsableRepository;
        this.missionInstallationRepository = missionInstallationRepository;
        this.interventionRepository = interventionRepository;
        this.rapportRepository = rapportRepository;
        this.photoRepository = photoRepository;
        this.attestationRepository = attestationRepository;
        this.checklistEquipementRepository = checklistEquipementRepository;
        this.checklistItemRepository = checklistItemRepository;
        this.observateurRepository = observateurRepository;
    }
    @Transactional(readOnly = true)
    public List<EtablissementResponse> getAll() {
        return etablissementRepository.findAllWithDetails()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EtablissementResponse getById(Integer id) {
        Etablissement e = etablissementRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable : " + id));
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    public EtablissementKpiResponse getKpis() {
        EtablissementKpiResponse kpi = new EtablissementKpiResponse();
        List<Etablissement> all = etablissementRepository.findAllWithDetails();

        kpi.setTotalEtablissements(all.size());
        kpi.setRegionsCouvertes(etablissementRepository.countRegionsCouvertes());
        kpi.setTotalBeneficiaires(
                all.stream()
                        .mapToLong(e -> e.getNombreBeneficiaires() == null ? 0 : e.getNombreBeneficiaires())
                        .sum()
        );
        kpi.setSansResponsable(etablissementRepository.countSansResponsable());

        return kpi;
    }

    @Transactional
    public EtablissementResponse create(EtablissementRequest request) {
        Etablissement e = new Etablissement();
        applyRequest(e, request);
        Etablissement saved = etablissementRepository.save(e);
        return toResponse(etablissementRepository.findByIdWithDetails(saved.getIdEtablissement()).orElseThrow());
    }

    @Transactional
    public EtablissementResponse update(Integer id, EtablissementRequest request) {
        Etablissement e = etablissementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable : " + id));
        applyRequest(e, request);
        Etablissement saved = etablissementRepository.save(e);
        return toResponse(etablissementRepository.findByIdWithDetails(saved.getIdEtablissement()).orElseThrow());
    }


    /**
     * Suppression sécurisée avec gestion de la cascade et avertissement préalable.
     */
    @Transactional
    public void delete(Integer id, boolean force) {
        Etablissement e = etablissementRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Établissement introuvable : " + id));

        List<MissionInstallation> missions = missionInstallationRepository.findByEtablissementIdEtablissement(id);

        List<Intervention> interventions = missions.stream()
                .flatMap(m -> interventionRepository.findByMissionIdMission(m.getIdMission()).stream())
                .collect(Collectors.toList());

        long nbMateriels = etablissementRepository.countMaterielsByEtablissementId(id);
        long nbMissions = missions.size();
        long nbInterventions = interventions.size();

        if ((nbMateriels > 0 || nbMissions > 0 || nbInterventions > 0) && !force) {
            throw new IllegalStateException("Attention : Cet établissement contient " + nbMateriels +
                    " matériel(s), " + nbMissions + " mission(s) d'installation et " + nbInterventions +
                    " intervention(s) associée(s). Tout ce qui est lié à cet établissement va être supprimé.");
        }

        if (force) {
            for (Intervention intervention : interventions) {
                rapportRepository.deleteByIntervention(intervention);
                photoRepository.deleteByIntervention(intervention);
                attestationRepository.deleteByIntervention(intervention);
                checklistEquipementRepository.deleteByIntervention(intervention);
            }

            for (MissionInstallation mission : missions) {
                rapportRepository.deleteByMission(mission);
            }

            interventionRepository.deleteAll(interventions);
            missionInstallationRepository.deleteAll(missions);
        }

        etablissementRepository.delete(e);
    }

    // ============================================================
    // --- Mapping helpers ---
    // ============================================================

    private void applyRequest(Etablissement e, EtablissementRequest request) {
        e.setReference(request.getReference());
        e.setDesignation(request.getDesignation());
        e.setType(request.getType());
        e.setLocalisationGps(request.getLocalisationGps());
        e.setNombreBeneficiaires(request.getNombreBeneficiaires());
        e.setTelephoneContact(request.getTelephoneContact());
        e.setEmailContact(request.getEmailContact());

        Commune commune = communeRepository.findById(request.getIdCommune())
                .orElseThrow(() -> new NoSuchElementException("Commune introuvable : " + request.getIdCommune()));
        e.setCommune(commune);

        e.setResponsable(resolveResponsable(request.getResponsable()));
    }

    private Responsable resolveResponsable(ResponsableDto dto) {
        if (dto == null || isBlank(dto.getNom())) {
            return null;
        }

        Responsable responsable;
        if (dto.getIdResponsable() != null) {
            responsable = responsableRepository.findById(dto.getIdResponsable())
                    .orElseGet(Responsable::new);
        } else {
            responsable = new Responsable();
        }

        responsable.setNom(dto.getNom());
        responsable.setPrenom(dto.getPrenom());
        responsable.setFonction(dto.getFonction());
        responsable.setTelephone(dto.getTelephone());
        responsable.setEmail(dto.getEmail());

        return responsableRepository.save(responsable);
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private EtablissementResponse toResponse(Etablissement e) {
        EtablissementResponse r = new EtablissementResponse();
        r.setIdEtablissement(e.getIdEtablissement());
        r.setReference(e.getReference());
        r.setDesignation(e.getDesignation());
        r.setType(e.getType());
        r.setLocalisationGps(e.getLocalisationGps());
        r.setNombreBeneficiaires(e.getNombreBeneficiaires());
        r.setTelephoneContact(e.getTelephoneContact());
        r.setEmailContact(e.getEmailContact());

        if (e.getCommune() != null) {
            r.setIdCommune(e.getCommune().getIdCommune());
            r.setCommuneNom(e.getCommune().getNom());

            if (e.getCommune().getProvince() != null) {
                r.setIdProvince(e.getCommune().getProvince().getIdProvince());
                r.setProvinceNom(e.getCommune().getProvince().getNom());

                if (e.getCommune().getProvince().getRegion() != null) {
                    r.setIdRegion(e.getCommune().getProvince().getRegion().getIdRegion());
                    r.setRegionNom(e.getCommune().getProvince().getRegion().getNom());
                }
            }
        }

        if (e.getResponsable() != null) {
            ResponsableDto rd = new ResponsableDto();
            rd.setIdResponsable(e.getResponsable().getIdResponsable());
            rd.setNom(e.getResponsable().getNom());
            rd.setPrenom(e.getResponsable().getPrenom());
            rd.setFonction(e.getResponsable().getPrenom()); // S'assurer que les getters/setters correspondent
            rd.setTelephone(e.getResponsable().getTelephone());
            rd.setEmail(e.getResponsable().getEmail());
            r.setResponsable(rd);
        }
        r.setNbFormateurs((int) observateurRepository.countByEtablissement_IdEtablissement(e.getIdEtablissement()));

        return r;
    }
}