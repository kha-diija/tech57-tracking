package com.example.backend.dto.technicien.Dashboard;

public class EtablissementDto {
    private Long id;
    private String nom;
    private String ville;
    private int interventions;
    private String etat;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public int getInterventions() { return interventions; }
    public void setInterventions(int interventions) { this.interventions = interventions; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }
}