package com.example.backend.dto.stock.dashboard;

public class LowStockItemResponse {
    private Integer idMateriel;
    private String nom;
    private String reference;
    private int quantiteDisponible;
    private int seuilAlerte;

    public Integer getIdMateriel() { return idMateriel; }
    public void setIdMateriel(Integer idMateriel) { this.idMateriel = idMateriel; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public int getQuantiteDisponible() { return quantiteDisponible; }
    public void setQuantiteDisponible(int quantiteDisponible) { this.quantiteDisponible = quantiteDisponible; }
    public int getSeuilAlerte() { return seuilAlerte; }
    public void setSeuilAlerte(int seuilAlerte) { this.seuilAlerte = seuilAlerte; }
}