package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MaterielRequest {

    @NotBlank(message = "La référence est obligatoire")
    private String reference;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String numeroSerie;

    // Optionnel : si absent, généré automatiquement à la création
    private String codeQr;

    // Optionnel : par défaut "Neuf" à la création
    private String etat;

    @NotNull(message = "La catégorie est obligatoire")
    private Integer idCategorie;

    private Integer idEtablissement;
}