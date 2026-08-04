package com.example.backend.dto.admin.intervention;

import java.time.LocalDateTime;

public class RetourMaterielDto {
    private Integer idRetour;
    private String materielReference;
    private Integer quantite;
    private String etatMateriel;
    private LocalDateTime dateRetour;

    public RetourMaterielDto() {}

    public Integer getIdRetour() { return idRetour; }
    public void setIdRetour(Integer idRetour) { this.idRetour = idRetour; }

    public String getMaterielReference() { return materielReference; }
    public void setMaterielReference(String materielReference) { this.materielReference = materielReference; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public String getEtatMateriel() { return etatMateriel; }
    public void setEtatMateriel(String etatMateriel) { this.etatMateriel = etatMateriel; }

    public LocalDateTime getDateRetour() { return dateRetour; }
    public void setDateRetour(LocalDateTime dateRetour) { this.dateRetour = dateRetour; }
}