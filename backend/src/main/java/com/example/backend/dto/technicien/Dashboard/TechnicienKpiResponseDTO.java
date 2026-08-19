package com.example.backend.dto.technicien.Dashboard;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicienKpiResponseDTO {
    // --- Existant, conservé ---
    private long missionsJour;
    private long etablissementsCount;
    private long enAttente;
    private List<MissionDto> missionsActuelles;
    private List<EtablissementDto> etablissementsAssignes;

    // --- NOUVEAU : basés sur la vraie logique de statut ---
    private long interventionsRealisees;   // Exécutée + Clôturée
    private long interventionsEnCours;
    private long interventionsEnRetard;
    private double tauxAvancementMoyen;
    private double tauxConformite;         // en %, type double (plus exploitable qu'une String)
    private double tempsMoyenInterventionMinutes;
    private long anomaliesDetectees;       // checklist non conforme

    // --- NOUVEAU : RG-07 / RG-08 ---
    private long quantiteMaterielSortie;
    private long quantiteMaterielRendue;
}