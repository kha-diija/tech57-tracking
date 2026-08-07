package com.example.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AchatMaterielDto {
    private Integer idAchat;
    private String numeroFacture;
    private String fournisseur;
    private Integer quantite;
    private Double prixUnitaireHt;
    private LocalDateTime dateAchat;

    private Integer idMateriel;
    private String materielReference;
    private String materielNom;

    private String acheteurNom; // nom + prénom, admin ou gestionnaire selon qui a créé
}