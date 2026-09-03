package com.example.backend.dto.admin.etablissement;

public class EtablissementKpiResponse {
    private long totalEtablissements;
    private long regionsCouvertes;
    private long totalBeneficiaires;
    private long totalBeneficiairesReel;
    private long sansResponsable;

    public long getTotalEtablissements() { return totalEtablissements; }
    public void setTotalEtablissements(long totalEtablissements) { this.totalEtablissements = totalEtablissements; }
    public long getRegionsCouvertes() { return regionsCouvertes; }
    public void setRegionsCouvertes(long regionsCouvertes) { this.regionsCouvertes = regionsCouvertes; }
    public long getTotalBeneficiaires() { return totalBeneficiaires; }
    public void setTotalBeneficiaires(long totalBeneficiaires) { this.totalBeneficiaires = totalBeneficiaires; }
    public long getTotalBeneficiairesReel() { return totalBeneficiairesReel; }
    public void setTotalBeneficiairesReel(long totalBeneficiairesReel) { this.totalBeneficiairesReel = totalBeneficiairesReel; }
    public long getSansResponsable() { return sansResponsable; }
    public void setSansResponsable(long sansResponsable) { this.sansResponsable = sansResponsable; }
}