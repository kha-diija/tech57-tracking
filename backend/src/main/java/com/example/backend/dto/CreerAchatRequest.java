package com.example.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreerAchatRequest {

    @NotNull(message = "Le matériel est obligatoire")
    private Integer idMateriel;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être supérieure à 0")
    private Integer quantite;

    private String fournisseur;
    private String numeroFacture;
    private Double prixUnitaireHt;
}