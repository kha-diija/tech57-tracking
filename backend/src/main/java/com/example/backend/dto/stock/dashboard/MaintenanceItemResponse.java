package com.example.backend.dto.stock.dashboard;

public class MaintenanceItemResponse {
    private Integer idMateriel;
    private String reference;
    private String nom;
    private String etat;
    private String categorie;
    private String etablissement;

    public Integer getIdMateriel() { return idMateriel; }
    public void setIdMateriel(Integer idMateriel) { this.idMateriel = idMateriel; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public String getEtablissement() { return etablissement; }
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }
}