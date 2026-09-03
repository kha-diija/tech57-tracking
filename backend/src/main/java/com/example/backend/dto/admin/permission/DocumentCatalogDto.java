package com.example.backend.dto.admin.permission;

public class DocumentCatalogDto {
    private Integer idSource;
    private String nomFichier;
    private String typeSource;

    public DocumentCatalogDto() {}

    public DocumentCatalogDto(Integer idSource, String nomFichier, String typeSource) {
        this.idSource = idSource;
        this.nomFichier = nomFichier;
        this.typeSource = typeSource;
    }

    public Integer getIdSource() { return idSource; }
    public void setIdSource(Integer idSource) { this.idSource = idSource; }
    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }
    public String getTypeSource() { return typeSource; }
    public void setTypeSource(String typeSource) { this.typeSource = typeSource; }
}