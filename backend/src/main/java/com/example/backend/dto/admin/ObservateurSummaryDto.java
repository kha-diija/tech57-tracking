package com.example.backend.dto.admin;

public class ObservateurSummaryDto {
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String typeClient;

    public ObservateurSummaryDto() {}

    public ObservateurSummaryDto(Integer id, String nom, String prenom, String email, String typeClient) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.typeClient = typeClient;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTypeClient() { return typeClient; }
    public void setTypeClient(String typeClient) { this.typeClient = typeClient; }
}