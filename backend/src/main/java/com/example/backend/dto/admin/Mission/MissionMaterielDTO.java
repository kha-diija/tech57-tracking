package com.example.backend.dto.admin.Mission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MissionMaterielDTO {
    private Integer idMateriel;
    private Integer quantite;
    private String statut;
    private String motifRejet;
}