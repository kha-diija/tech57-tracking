package com.example.backend.dto.technicien;

public class MaterielSimpleDto {
    private Integer idMateriel;
    private String nom;
    private String reference;

    public MaterielSimpleDto(Integer idMateriel, String nom, String reference) {
        this.idMateriel = idMateriel;
        this.nom = nom;
        this.reference = reference;
    }

    public Integer getIdMateriel() { return idMateriel; }
    public String getNom() { return nom; }
    public String getReference() { return reference; }
}