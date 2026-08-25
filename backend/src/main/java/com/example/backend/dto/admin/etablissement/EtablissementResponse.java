package com.example.backend.dto.admin.etablissement;

public class EtablissementResponse {
    private Integer idEtablissement;
    private String reference;
    private String designation;
    private String type;
    private String localisationGps;
    private Integer nombreBeneficiaires;
    private Integer nombreBeneficiairesReel;
    private String telephoneContact;
    private String emailContact;

    private Integer idCommune;
    private String communeNom;
    private Integer idProvince;
    private String provinceNom;
    private Integer idRegion;
    private String regionNom;

    private ResponsableDto responsable; // peut être null

    public Integer getIdEtablissement() { return idEtablissement; }
    public void setIdEtablissement(Integer idEtablissement) { this.idEtablissement = idEtablissement; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLocalisationGps() { return localisationGps; }
    public void setLocalisationGps(String localisationGps) { this.localisationGps = localisationGps; }
    public Integer getNombreBeneficiaires() { return nombreBeneficiaires; }
    public void setNombreBeneficiaires(Integer nombreBeneficiaires) { this.nombreBeneficiaires = nombreBeneficiaires; }
    public String getTelephoneContact() { return telephoneContact; }
    public void setTelephoneContact(String telephoneContact) { this.telephoneContact = telephoneContact; }
    public String getEmailContact() { return emailContact; }
    public void setEmailContact(String emailContact) { this.emailContact = emailContact; }
    public Integer getIdCommune() { return idCommune; }
    public void setIdCommune(Integer idCommune) { this.idCommune = idCommune; }
    public String getCommuneNom() { return communeNom; }
    public void setCommuneNom(String communeNom) { this.communeNom = communeNom; }
    public Integer getIdProvince() { return idProvince; }
    public void setIdProvince(Integer idProvince) { this.idProvince = idProvince; }
    public String getProvinceNom() { return provinceNom; }
    public void setProvinceNom(String provinceNom) { this.provinceNom = provinceNom; }
    public Integer getIdRegion() { return idRegion; }
    public void setIdRegion(Integer idRegion) { this.idRegion = idRegion; }
    public String getRegionNom() { return regionNom; }
    public void setRegionNom(String regionNom) { this.regionNom = regionNom; }
    public ResponsableDto getResponsable() { return responsable; }
    public void setResponsable(ResponsableDto responsable) { this.responsable = responsable; }

    public Integer getNombreBeneficiairesReel() { return nombreBeneficiairesReel; }
    public void setNombreBeneficiairesReel(Integer nombreBeneficiairesReel) { this.nombreBeneficiairesReel = nombreBeneficiairesReel; }

    private Integer nbFormateurs;

    public Integer getNbFormateurs() { return nbFormateurs; }
    public void setNbFormateurs(Integer nbFormateurs) { this.nbFormateurs = nbFormateurs; }
}