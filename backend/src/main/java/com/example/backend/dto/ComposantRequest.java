package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ComposantRequest {

    @NotBlank(message = "Le nom du composant est obligatoire")
    private String nom;

    private String reference;      // généré automatiquement si absent
    private String numeroSerie;
    private String codeQr;         // si absent, hérite du QR du kit parent
    private Integer quantiteComposant = 1;
}