package com.example.backend.dto.admin.etablissement;

public class ProvinceResponse {
    private Integer idProvince;
    private String nom;
    private String code;
    private Integer idRegion;

    public ProvinceResponse() {}
    public ProvinceResponse(Integer idProvince, String nom, String code, Integer idRegion) {
        this.idProvince = idProvince;
        this.nom = nom;
        this.code = code;
        this.idRegion = idRegion;
    }

    public Integer getIdProvince() { return idProvince; }
    public void setIdProvince(Integer idProvince) { this.idProvince = idProvince; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getIdRegion() { return idRegion; }
    public void setIdRegion(Integer idRegion) { this.idRegion = idRegion; }
}