package com.example.backend.dto.admin.etablissement;

public class ResponsableDto {
    private Integer idResponsable; // null si nouveau responsable à créer
    private String nom;
    private String prenom;
    private String fonction;
    private String telephone;
    private String email;

    public Integer getIdResponsable() { return idResponsable; }
    public void setIdResponsable(Integer idResponsable) { this.idResponsable = idResponsable; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}