package com.example.backend.dto.admin.permission;

public class VideoCatalogDto {
    private Integer idVideo;
    private String titre;
    private String fournisseur;
    private Integer dureeSecondes;

    public VideoCatalogDto() {}

    public VideoCatalogDto(Integer idVideo, String titre, String fournisseur, Integer dureeSecondes) {
        this.idVideo = idVideo;
        this.titre = titre;
        this.fournisseur = fournisseur;
        this.dureeSecondes = dureeSecondes;
    }

    public Integer getIdVideo() { return idVideo; }
    public void setIdVideo(Integer idVideo) { this.idVideo = idVideo; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getFournisseur() { return fournisseur; }
    public void setFournisseur(String fournisseur) { this.fournisseur = fournisseur; }
    public Integer getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(Integer dureeSecondes) { this.dureeSecondes = dureeSecondes; }
}