package com.example.backend.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CategorieMaterielDTO {
    private Integer idCategorie;
    private String nom;
    private Boolean estKit;
}