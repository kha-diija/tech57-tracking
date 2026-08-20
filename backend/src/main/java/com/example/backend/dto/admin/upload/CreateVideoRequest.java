package com.example.backend.dto.admin.upload;

import jakarta.validation.constraints.NotBlank;

public class CreateVideoRequest {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    private String description;

    @NotBlank(message = "L'URL de la vidéo est obligatoire")
    private String urlVideo;

    private String urlMiniature;
    private String fournisseur = "YouTube";
    private Integer dureeSecondes;
    private Integer idCategorie;
    private Integer idMateriel;

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getUrlVideo() { return urlVideo; }
    public void setUrlVideo(String urlVideo) { this.urlVideo = urlVideo; }
    public String getUrlMiniature() { return urlMiniature; }
    public void setUrlMiniature(String urlMiniature) { this.urlMiniature = urlMiniature; }
    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
    public Integer getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(Integer dureeSecondes) { this.dureeSecondes = dureeSecondes; }
    public Integer getIdCategorie() { return idCategorie; }
    public void setIdCategorie(Integer idCategorie) { this.idCategorie = idCategorie; }
    public Integer getIdMateriel() { return idMateriel; }
    public void setIdMateriel(Integer idMateriel) { this.idMateriel = idMateriel; }
}