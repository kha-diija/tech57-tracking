package com.example.backend.service.technicien;

import com.example.backend.dto.technicien.Dashboard.EtablissementDto;
import com.example.backend.dto.technicien.Dashboard.MissionDto;
import com.example.backend.dto.technicien.Dashboard.TechnicienKpiResponseDTO;
import com.example.backend.entity.*;
import com.example.backend.repository.admin.ChecklistEquipementRepository;
import com.example.backend.repository.admin.ChecklistItemRepository;
import com.example.backend.repository.admin.RetourMaterielRepository;
import com.example.backend.repository.admin.SortieMaterielRepository;
import com.example.backend.repository.technicien.DashboardTechnicienRepository;
import com.example.backend.entity.InterventionStatutHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechnicienDashboardService {

    private final DashboardTechnicienRepository dashboardTechnicienRepository;
    private final SortieMaterielRepository sortieMaterielRepository;
    private final RetourMaterielRepository retourMaterielRepository;
    private final ChecklistEquipementRepository checklistEquipementRepository;
    private final ChecklistItemRepository checklistItemRepository;

    public TechnicienKpiResponseDTO calculerKpisPourTechnicien(String emailTechnicien) {
        // 1 seule requête avec fetch join pour les interventions
        List<Intervention> interventions = dashboardTechnicienRepository.findInterventionsByTechnicienEmail(emailTechnicien);

        long total = interventions.size();
        long enCours = 0, enRetard = 0, realisees = 0;
        double sommeAvancement = 0;
        double sommeDureeMinutes = 0;
        long nombreVisitesAvecDuree = 0;
        long anomalies = 0;
        long quantiteSortie = 0;
        long quantiteRendue = 0;

        // Récupérer les IDs des interventions pour les requêtes groupées
        List<Integer> interventionIds = interventions.stream()
                .map(Intervention::getIdIntervention)
                .collect(Collectors.toList());

        // ⚡ 1 seule requête pour TOUTES les checklists
        List<ChecklistEquipement> allChecklists = checklistEquipementRepository.findByInterventionIds(interventionIds);
        Map<Integer, List<ChecklistEquipement>> checklistsByIntervention = allChecklists.stream()
                .collect(Collectors.groupingBy(
                        chk -> chk.getIntervention().getIdIntervention()
                ));

        // Récupérer les IDs des checklists
        List<Integer> checklistIds = allChecklists.stream()
                .map(ChecklistEquipement::getIdChecklist)
                .collect(Collectors.toList());

        // ⚡ 1 seule requête pour TOUS les items de checklist
        Map<Integer, List<ChecklistItem>> itemsByChecklist = new java.util.HashMap<>();
        if (!checklistIds.isEmpty()) {
            itemsByChecklist = checklistItemRepository.findByChecklistIds(checklistIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getChecklist().getIdChecklist()
                    ));
        }

        // ⚡ 1 seule requête pour TOUTES les sorties
        Map<Integer, List<SortieMateriel>> sortiesByIntervention = new java.util.HashMap<>();
        if (!interventionIds.isEmpty()) {
            sortiesByIntervention = sortieMaterielRepository.findByInterventionIds(interventionIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            s -> s.getIntervention().getIdIntervention()
                    ));
        }

        // ⚡ 1 seule requête pour TOUS les retours
        Map<Integer, List<RetourMateriel>> retoursByIntervention = new java.util.HashMap<>();
        if (!interventionIds.isEmpty()) {
            retoursByIntervention = retourMaterielRepository.findByInterventionIds(interventionIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            r -> r.getIntervention().getIdIntervention()
                    ));
        }

        for (Intervention it : interventions) {
            Integer interventionId = it.getIdIntervention();

            String statut = InterventionStatutHelper.calculerStatutAffiche(it);

            switch (statut) {
                case "En cours" -> enCours++;
                case "En retard" -> enRetard++;
                case "Exécutée", "Clôturée" -> realisees++;
                default -> {}
            }

            double avancement = it.getTauxAvancement() != null ? it.getTauxAvancement() : 0.0;
            sommeAvancement += avancement;

            // Temps moyen d'installation (déjà chargé par fetch join)
            if (it.getCheckInOuts() != null) {
                for (CheckInOut v : it.getCheckInOuts()) {
                    if (v.getDureeMinutes() != null) {
                        sommeDureeMinutes += v.getDureeMinutes();
                        nombreVisitesAvecDuree++;
                    }
                }
            }

            // ✅ Utiliser les maps pré-chargées pour les checklists
            List<ChecklistEquipement> checklists = checklistsByIntervention.getOrDefault(interventionId, new ArrayList<>());
            for (ChecklistEquipement chk : checklists) {
                List<ChecklistItem> items = itemsByChecklist.getOrDefault(chk.getIdChecklist(), new ArrayList<>());
                anomalies += items.stream().filter(item -> Boolean.FALSE.equals(item.getConforme())).count();
            }

            // ✅ Utiliser les maps pré-chargées pour les sorties
            List<SortieMateriel> sorties = sortiesByIntervention.getOrDefault(interventionId, new ArrayList<>());
            for (SortieMateriel s : sorties) {
                if (s.getDetails() != null) {
                    quantiteSortie += s.getDetails().stream()
                            .mapToLong(d -> d.getQuantite() != null ? d.getQuantite() : 0)
                            .sum();
                }
            }

            // ✅ Utiliser les maps pré-chargées pour les retours
            List<RetourMateriel> retours = retoursByIntervention.getOrDefault(interventionId, new ArrayList<>());
            quantiteRendue += retours.stream()
                    .mapToLong(r -> r.getQuantite() != null ? r.getQuantite() : 0)
                    .sum();
        }

        double tauxAvancementMoyen = total > 0 ? Math.round((sommeAvancement / total) * 10.0) / 10.0 : 0.0;
        double tauxConformite = total > 0 ? Math.round((realisees * 100.0 / total) * 10.0) / 10.0 : 0.0;
        double tempsMoyenMinutes = nombreVisitesAvecDuree > 0
                ? Math.round((sommeDureeMinutes / nombreVisitesAvecDuree) * 10.0) / 10.0 : 0.0;

        // Mapping vers MissionDto
        List<MissionDto> missionsActuelles = interventions.stream().map(i -> {
            MissionDto dto = new MissionDto();
            dto.setId(i.getIdIntervention() != null ? i.getIdIntervention().longValue() : 0L);

            if (i.getMission() != null) {
                dto.setTitre("Mission #" + i.getMission().getIdMission());
                dto.setEtablissement(i.getMission().getEtablissement() != null
                        ? i.getMission().getEtablissement().getDesignation() : "Non spécifié");
            } else {
                dto.setTitre("Intervention #" + i.getIdIntervention());
                dto.setEtablissement("Non spécifié");
            }

            dto.setStatut(InterventionStatutHelper.calculerStatutAffiche(i));
            dto.setHoraire(i.getDatePrevue() != null ? i.getDatePrevue().toString() : null);
            dto.setUrgence("En retard".equals(dto.getStatut()) ? "Urgent" : "Normale");

            return dto;
        }).collect(Collectors.toList());

        List<EtablissementDto> etablissementsAssignes = interventions.stream()
                .filter(i -> i.getMission() != null && i.getMission().getEtablissement() != null)
                .map(i -> i.getMission().getEtablissement())
                .distinct()
                .map(e -> {
                    EtablissementDto dto = new EtablissementDto();
                    dto.setId(e.getIdEtablissement() != null ? e.getIdEtablissement().longValue() : 0L);
                    dto.setNom(e.getDesignation());
                    dto.setVille(e.getReference());
                    long nbInterventions = interventions.stream()
                            .filter(i -> i.getMission() != null && i.getMission().getEtablissement() != null
                                    && i.getMission().getEtablissement().getIdEtablissement().equals(e.getIdEtablissement()))
                            .count();
                    dto.setInterventions((int) nbInterventions);
                    return dto;
                }).collect(Collectors.toList());

        return TechnicienKpiResponseDTO.builder()
                .missionsJour(enCours)
                .etablissementsCount(etablissementsAssignes.size())
                .enAttente(enRetard)
                .missionsActuelles(missionsActuelles)
                .etablissementsAssignes(etablissementsAssignes)
                .interventionsRealisees(realisees)
                .interventionsEnCours(enCours)
                .interventionsEnRetard(enRetard)
                .tauxAvancementMoyen(tauxAvancementMoyen)
                .tauxConformite(tauxConformite)
                .tempsMoyenInterventionMinutes(tempsMoyenMinutes)
                .anomaliesDetectees(anomalies)
                .quantiteMaterielSortie(quantiteSortie)
                .quantiteMaterielRendue(quantiteRendue)
                .build();
    }
}