package com.example.backend.dto.admin.etablissement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommuneDTO {
    private Integer idCommune;
    private String nom;
    private String code;
    private Integer idProvince;
    private String provinceNom;
}