package com.example.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

    @NotBlank(message = "Le token est obligatoire")
    private String token;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String nouveauMotDePasse;

    public ResetPasswordRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNouveauMotDePasse() { return nouveauMotDePasse; }
    public void setNouveauMotDePasse(String nouveauMotDePasse) { this.nouveauMotDePasse = nouveauMotDePasse; }
}