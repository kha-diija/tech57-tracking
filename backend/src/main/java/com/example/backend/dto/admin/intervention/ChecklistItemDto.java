package com.example.backend.dto.admin.intervention;

public class ChecklistItemDto {
    private Integer idItem;
    private String materielReference;
    private Integer quantite;
    private String etatConstate;
    private Boolean conforme;
    private Integer idMateriel;

    // --- AJOUT DU CHAMP NOM ---
    private String nom;

    public ChecklistItemDto() {}

    public Integer getIdItem() { return idItem; }
    public void setIdItem(Integer idItem) { this.idItem = idItem; }

    public String getMaterielReference() { return materielReference; }
    public void setMaterielReference(String materielReference) { this.materielReference = materielReference; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getEtatConstate() { return etatConstate; }
    public void setEtatConstate(String etatConstate) { this.etatConstate = etatConstate; }

    public Boolean getConforme() { return conforme; }
    public void setConforme(Boolean conforme) { this.conforme = conforme; }

    public Integer getIdMateriel() { return idMateriel; }
    public void setIdMateriel(Integer idMateriel) { this.idMateriel = idMateriel; }

    // --- NOUVEAUX GETTER ET SETTER POUR NOM ---
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}