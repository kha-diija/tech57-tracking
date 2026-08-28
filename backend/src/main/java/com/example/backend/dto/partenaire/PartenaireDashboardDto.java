package com.example.backend.dto.partenaire;

import java.util.List;

public class PartenaireDashboardDto {
    private String nomProvince;
    private long nombreEtablissements;
    private long totalBeneficiaires;
    private double avancementGlobal;
    private List<EtablissementAvancementDto> etablissements;

    public PartenaireDashboardDto(String nomProvince, long nombreEtablissements, long totalBeneficiaires,
                                  double avancementGlobal, List<EtablissementAvancementDto> etablissements) {
        this.nomProvince = nomProvince;
        this.nombreEtablissements = nombreEtablissements;
        this.totalBeneficiaires = totalBeneficiaires;
        this.avancementGlobal = avancementGlobal;
        this.etablissements = etablissements;
    }
    public String getNomProvince() { return nomProvince; }
    public long getNombreEtablissements() { return nombreEtablissements; }
    public long getTotalBeneficiaires() { return totalBeneficiaires; }
    public double getAvancementGlobal() { return avancementGlobal; }
    public List<EtablissementAvancementDto> getEtablissements() { return etablissements; }
}