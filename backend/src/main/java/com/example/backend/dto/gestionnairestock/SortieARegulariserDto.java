package com.example.backend.dto.gestionnairestock;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SortieARegulariserDto {
    private Integer idSortie;
    private LocalDateTime dateSortie;
    private String missionReference;
    private Integer technicienId;
    private String technicienNom;
    private List<LigneSortieDto> lignes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LigneSortieDto {
        private Integer idMateriel;
        private String materielReference;
        private String materielNom;
        private Integer quantiteSortie;
    }
}