package com.example.backend.dto.gestionnairestock;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortieMaterielDetailDto {
    private Integer idDetail;
    private Integer idMateriel;
    private String materielReference;
    private String materielNom;
    private Integer quantiteDemandee;
    private Integer stockDisponible; // pour affichage badge vert/rouge côté front
}