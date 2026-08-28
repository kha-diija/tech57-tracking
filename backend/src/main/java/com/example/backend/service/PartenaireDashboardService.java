package com.example.backend.service;

import com.example.backend.dto.partenaire.EtablissementAvancementDto;
import com.example.backend.dto.partenaire.PartenaireDashboardDto;
import com.example.backend.entity.Etablissement;
import com.example.backend.entity.Partenaire;
import com.example.backend.repository.admin.EtablissementRepository;
import com.example.backend.repository.InterventionRepositoryy;
import com.example.backend.repository.PartenaireRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartenaireDashboardService {

    private final PartenaireRepository partenaireRepository;
    private final EtablissementRepository etablissementRepository;
    private final InterventionRepositoryy interventionRepositoryy;

    public PartenaireDashboardService(PartenaireRepository partenaireRepository,
                                      EtablissementRepository etablissementRepository,
                                      InterventionRepositoryy interventionRepositoryy) {
        this.partenaireRepository = partenaireRepository;
        this.etablissementRepository = etablissementRepository;
        this.interventionRepositoryy = interventionRepositoryy;
    }

    public PartenaireDashboardDto getDashboard() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Partenaire partenaire = partenaireRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Partenaire introuvable"));

        Integer idProvince = partenaire.getProvince().getIdProvince();

        // Nécessite dans EtablissementRepository :
        // List<Etablissement> findByCommuneProvinceIdProvince(Integer idProvince);
        List<Etablissement> etablissements = etablissementRepository
                .findByCommuneProvinceIdProvince(idProvince);

        List<EtablissementAvancementDto> dtos = etablissements.stream()
                .map(this::toDto)
                .toList();

        long totalBeneficiaires = etablissements.stream()
                .mapToLong(e -> e.getNombreBeneficiaires() == null ? 0 : e.getNombreBeneficiaires())
                .sum();

        Double avancementProvince = interventionRepositoryy.findAvgTauxAvancementByProvince(idProvince);

        return new PartenaireDashboardDto(
                partenaire.getProvince().getNom(),
                etablissements.size(),
                totalBeneficiaires,
                avancementProvince != null ? avancementProvince : 0.0,
                dtos
        );
    }

    private EtablissementAvancementDto toDto(Etablissement e) {
        Double avgTaux = interventionRepositoryy.findAvgTauxAvancementByEtablissement(e.getIdEtablissement());

        return new EtablissementAvancementDto(
                e.getIdEtablissement(),
                e.getDesignation(),
                e.getCommune().getNom(),
                e.getNombreBeneficiaires(),
                e.getMateriels().size(),
                avgTaux != null ? avgTaux : 0.0
        );
    }
}