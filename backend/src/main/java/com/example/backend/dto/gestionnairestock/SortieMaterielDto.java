package com.example.backend.dto.gestionnairestock;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortieMaterielDto {
    private Integer idSortie;
    private LocalDateTime dateSortie;
    private String lieuIntervention;
    private String statut;
    private String motifRejet;

    private Integer technicienId;
    private String technicienNom;

    private Integer interventionId;
    private String missionReference;

    private List<SortieMaterielDetailDto> details;
}