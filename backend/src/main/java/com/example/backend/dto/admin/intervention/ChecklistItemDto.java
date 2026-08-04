package com.example.backend.dto.admin.intervention;

public class ChecklistItemDto {
    private Integer idItem;
    private String materielReference;
    private Integer quantite;
    private String etatConstate;
    private Boolean conforme;

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
}