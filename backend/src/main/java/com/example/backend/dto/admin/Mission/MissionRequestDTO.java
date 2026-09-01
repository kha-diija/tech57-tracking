package com.example.backend.dto.admin.Mission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionRequestDTO {

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String statut; // "Planifiée" par défaut si vide

    private Double budgetPropose;

    @NotNull(message = "L'établissement est obligatoire")
    private Integer idEtablissement;

    @NotNull(message = "L'administrateur est obligatoire")
    private Integer idAdministrateur;

    private Integer idEquipe; // Optionnel
    // ✅ NOUVEAU : ID de la province (pour filtrage)
    private Integer idProvince;

    // ✅ NOUVEAU : ID de la commune (pour filtrage)
    private Integer idCommune;
    // ✅ NOUVEAU : Liste des matériels avec quantités
    private List<MissionMaterielDTO> materiels;
}