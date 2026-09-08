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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // 1 requête : établissements + commune déjà chargée (fetch join)
        List<Etablissement> etablissements = etablissementRepository
                .findByCommuneProvinceIdProvinceWithCommune(idProvince);

        List<Integer> ids = etablissements.stream().map(Etablissement::getIdEtablissement).toList();

        // 1 requête pour TOUS les comptes de matériel (au lieu de N)
        Map<Integer, Long> nbMaterielsParEtablissement = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : etablissementRepository.countMaterielsByEtablissementIds(ids)) {
                nbMaterielsParEtablissement.put((Integer) row[0], (Long) row[1]);
            }
        }

        // 1 requête pour TOUTES les moyennes de taux d'avancement (au lieu de N)
        Map<Integer, Double> avgTauxParEtablissement = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] row : interventionRepositoryy.findAvgTauxAvancementByEtablissementIds(ids)) {
                avgTauxParEtablissement.put((Integer) row[0], (Double) row[1]);
            }
        }

        List<EtablissementAvancementDto> dtos = etablissements.stream()
                .map(e -> new EtablissementAvancementDto(
                        e.getIdEtablissement(),
                        e.getDesignation(),
                        e.getCommune().getNom(),
                        e.getNombreBeneficiaires(),
                        nbMaterielsParEtablissement.getOrDefault(e.getIdEtablissement(), 0L).intValue(),
                        avgTauxParEtablissement.getOrDefault(e.getIdEtablissement(), 0.0)
                ))
                .toList();

        long totalBeneficiaires = etablissements.stream()
                .mapToLong(e -> e.getNombreBeneficiaires() == null ? 0 : e.getNombreBeneficiaires())
                .sum();

        // 1 requête pour la moyenne province
        Double avancementProvince = interventionRepositoryy.findAvgTauxAvancementByProvince(idProvince);

        return new PartenaireDashboardDto(
                partenaire.getProvince().getNom(),
                etablissements.size(),
                totalBeneficiaires,
                avancementProvince != null ? avancementProvince : 0.0,
                dtos
        );
    }
}