package com.example.backend.dto.admin.intervention;

public class PhotoDto {
    private Integer id;
    private String cheminFichier;
    private String typePhoto;

    public PhotoDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }

    public String getTypePhoto() { return typePhoto; }
    public void setTypePhoto(String typePhoto) { this.typePhoto = typePhoto; }
}