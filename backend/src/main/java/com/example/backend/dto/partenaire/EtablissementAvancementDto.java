package com.example.backend.dto.partenaire;

public class EtablissementAvancementDto {
    private Integer idEtablissement;
    private String designation;
    private String commune;
    private Integer nombreBeneficiaires;
    private long nombreMateriels;
    private double pourcentageAvancement; // à brancher sur vos entités Mission/Intervention

    public EtablissementAvancementDto(Integer idEtablissement, String designation, String commune,
                                      Integer nombreBeneficiaires, long nombreMateriels,
                                      double pourcentageAvancement) {
        this.idEtablissement = idEtablissement;
        this.designation = designation;
        this.commune = commune;
        this.nombreBeneficiaires = nombreBeneficiaires;
        this.nombreMateriels = nombreMateriels;
        this.pourcentageAvancement = pourcentageAvancement;
    }
    // getters uniquement (DTO en lecture seule)
    public Integer getIdEtablissement() { return idEtablissement; }
    public String getDesignation() { return designation; }
    public String getCommune() { return commune; }
    public Integer getNombreBeneficiaires() { return nombreBeneficiaires; }
    public long getNombreMateriels() { return nombreMateriels; }
    public double getPourcentageAvancement() { return pourcentageAvancement; }
}