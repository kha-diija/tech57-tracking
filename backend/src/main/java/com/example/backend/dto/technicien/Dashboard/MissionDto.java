package com.example.backend.dto.technicien.Dashboard;

public class MissionDto {
    private Long id;
    private String titre;
    private String etablissement;
    private String horaire;
    private String urgence;
    private String statut;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getEtablissement() { return etablissement; }
    public void setEtablissement(String etablissement) { this.etablissement = etablissement; }

    public String getHoraire() { return horaire; }
    public void setHoraire(String horaire) { this.horaire = horaire; }

    public String getUrgence() { return urgence; }
    public void setUrgence(String urgence) { this.urgence = urgence; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}