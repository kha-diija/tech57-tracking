package com.example.backend.dto.gestionuser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRequestDto {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne doit pas dépasser 150 caractères")
    private String email;

    @Size(max = 30, message = "Le téléphone ne doit pas dépasser 30 caractères")
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, max = 100, message = "Le mot de passe doit contenir entre 8 et 100 caractères")
    private String motDePasse;

    @NotBlank(message = "Le type d'utilisateur est obligatoire")
    @Pattern(
            regexp = "ADMINISTRATEUR|TECHNICIEN|OBSERVATEUR|GESTIONNAIRE_STOCK",
            message = "Type d'utilisateur invalide"
    )
    private String typeUtilisateur;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getTypeUtilisateur() {
        return typeUtilisateur;
    }

    public void setTypeUtilisateur(String typeUtilisateur) {
        this.typeUtilisateur = typeUtilisateur;
    }
}
