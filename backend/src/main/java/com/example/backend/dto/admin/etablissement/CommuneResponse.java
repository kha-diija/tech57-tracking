package com.example.backend.dto.admin.etablissement;

public class CommuneResponse {
    private Integer idCommune;
    private String nom;
    private String code;
    private Integer idProvince;
    private String nomProvince; // ✅ NOUVEAU CHAMP

    public CommuneResponse() {}

    // ✅ MODIFICATION DU CONSTRUCTEUR (ajout du nomProvince)
    public CommuneResponse(Integer idCommune, String nom, String code, Integer idProvince, String nomProvince) {
        this.idCommune = idCommune;
        this.nom = nom;
        this.code = code;
        this.idProvince = idProvince;
        this.nomProvince = nomProvince;
    }

    public Integer getIdCommune() { return idCommune; }
    public void setIdCommune(Integer idCommune) { this.idCommune = idCommune; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getIdProvince() { return idProvince; }
    public void setIdProvince(Integer idProvince) { this.idProvince = idProvince; }
    public String getNomProvince() { return nomProvince; } // ✅ GETTER
    public void setNomProvince(String nomProvince) { this.nomProvince = nomProvince; } // ✅ SETTER
}