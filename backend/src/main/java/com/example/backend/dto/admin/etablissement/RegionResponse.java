package com.example.backend.dto.admin.etablissement;

public class RegionResponse {
    private Integer idRegion;
    private String nom;
    private String code;

    public RegionResponse() {}
    public RegionResponse(Integer idRegion, String nom, String code) {
        this.idRegion = idRegion;
        this.nom = nom;
        this.code = code;
    }

    public Integer getIdRegion() { return idRegion; }
    public void setIdRegion(Integer idRegion) { this.idRegion = idRegion; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}