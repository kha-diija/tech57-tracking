package com.example.backend.dto.admin.intervention;

import java.time.LocalDateTime;

public class SortieMaterielDto {
    private Integer idSortie;
    private String materielReference;
    private Integer quantite;
    private LocalDateTime dateSortie;

    public SortieMaterielDto() {}

    public Integer getIdSortie() { return idSortie; }
    public void setIdSortie(Integer idSortie) { this.idSortie = idSortie; }

    public String getMaterielReference() { return materielReference; }
    public void setMaterielReference(String materielReference) { this.materielReference = materielReference; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public LocalDateTime getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDateTime dateSortie) { this.dateSortie = dateSortie; }
}