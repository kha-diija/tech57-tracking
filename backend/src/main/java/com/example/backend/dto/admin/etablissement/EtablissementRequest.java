package com.example.backend.dto.admin.etablissement;

public class EtablissementRequest {
    private String reference;
    private String designation;
    private String type;
    private String localisationGps; // format "lat,lng"
    private Integer nombreBeneficiaires;
    private String telephoneContact;
    private String emailContact;
    private Integer idCommune;
    private ResponsableDto responsable; // peut être null

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
    public ResponsableDto getResponsable() { return responsable; }
    public void setResponsable(ResponsableDto responsable) { this.responsable = responsable; }
}