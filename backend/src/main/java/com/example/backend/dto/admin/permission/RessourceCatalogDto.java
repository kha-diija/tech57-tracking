package com.example.backend.dto.admin.permission;

public class RessourceCatalogDto {
    private Integer idRessource;
    private String titre;
    private String type;

    public RessourceCatalogDto() {}

    public RessourceCatalogDto(Integer idRessource, String titre, String type) {
        this.idRessource = idRessource;
        this.titre = titre;
        this.type = type;
    }

    public Integer getIdRessource() { return idRessource; }
    public void setIdRessource(Integer idRessource) { this.idRessource = idRessource; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}