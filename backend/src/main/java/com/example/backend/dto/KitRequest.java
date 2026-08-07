package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class KitRequest {

    @NotBlank(message = "La référence du kit est obligatoire")
    private String reference;

    @NotBlank(message = "Le nom du kit est obligatoire")
    private String nom;

    private String numeroSerie;
    private String codeQr;

    @NotNull(message = "La catégorie (doit avoir est_kit = true) est obligatoire")
    private Integer idCategorie;

    private Integer idEtablissement;

    // Si vrai (défaut), chaque composant hérite du code QR du kit.
    // Si faux, chaque composant reçoit son propre code QR généré.
    private Boolean composantsHeritentQr = true;

    // Optionnel : noms des composants à créer automatiquement.
    // Si null/vide, la liste standard du Kit VEX GO est utilisée :
    // Guide d'utilisation, Sachet des matériaux, Câbles USB, Chargeur, Batterie VEX GO
    private List<String> composants;
}