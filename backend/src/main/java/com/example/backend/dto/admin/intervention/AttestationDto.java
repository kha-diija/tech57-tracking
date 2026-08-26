package com.example.backend.dto.admin.intervention;

import java.time.LocalDateTime;

public class AttestationDto {
    private Integer id;
    private LocalDateTime dateSignature;
    private String signatureNumerique;
    private String nomSignataire;
    private Boolean valide;
    private String cheminFichier;

    public AttestationDto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDateTime dateSignature) { this.dateSignature = dateSignature; }

    public String getSignatureNumerique() { return signatureNumerique; }
    public void setSignatureNumerique(String signatureNumerique) { this.signatureNumerique = signatureNumerique; }

    public String getNomSignataire() { return nomSignataire; }
    public void setNomSignataire(String nomSignataire) { this.nomSignataire = nomSignataire; }

    public Boolean getValide() { return valide; }
    public void setValide(Boolean valide) { this.valide = valide; }

    public String getCheminFichier() { return cheminFichier; }
    public void setCheminFichier(String cheminFichier) { this.cheminFichier = cheminFichier; }
}