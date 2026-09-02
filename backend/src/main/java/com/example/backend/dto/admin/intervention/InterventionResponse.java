package com.example.backend.dto.admin.intervention;

import java.time.LocalDateTime;
import java.util.List;

public class InterventionResponse {

    private Integer id;
    private LocalDateTime datePrevue; // <-- Ajouté
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Double tauxAvancement;
    private Integer numeroVisite;
    private String statut;
    private String localisationGps;
    private Integer missionId;
    private String missionReference;
    private String etablissementDesignation;
    private String etablissementReference; // ✅ AJOUT
    private Integer technicienId;
    private String technicienNom;

    private List<PhotoDto> photos;
    private AttestationDto attestation;
    private List<SortieMaterielDto> sortiesMateriel;
    private List<RetourMaterielDto> retoursMateriel;
    private List<ChecklistItemDto> checklistItems;

    private List<CheckInOutDto> checkInOuts; // <-- Ajouté pour l'historique des visites multiples

    public InterventionResponse() {}

    // Getters et Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDatePrevue() { return datePrevue; }
    public void setDatePrevue(LocalDateTime datePrevue) { this.datePrevue = datePrevue; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public Double getTauxAvancement() { return tauxAvancement; }
    public void setTauxAvancement(Double tauxAvancement) { this.tauxAvancement = tauxAvancement; }

    public Integer getNumeroVisite() { return numeroVisite; }
    public void setNumeroVisite(Integer numeroVisite) { this.numeroVisite = numeroVisite; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getLocalisationGps() { return localisationGps; }
    public void setLocalisationGps(String localisationGps) { this.localisationGps = localisationGps; }

    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }

    public String getMissionReference() { return missionReference; }
    public void setMissionReference(String missionReference) { this.missionReference = missionReference; }

    public String getEtablissementDesignation() { return etablissementDesignation; }
    public void setEtablissementDesignation(String etablissementDesignation) { this.etablissementDesignation = etablissementDesignation; }

    public String getEtablissementReference() { return etablissementReference; } // ✅ AJOUT
    public void setEtablissementReference(String etablissementReference) { this.etablissementReference = etablissementReference; } // ✅ AJOUT

    public Integer getTechnicienId() { return technicienId; }
    public void setTechnicienId(Integer technicienId) { this.technicienId = technicienId; }

    public String getTechnicienNom() { return technicienNom; }
    public void setTechnicienNom(String technicienNom) { this.technicienNom = technicienNom; }

    public List<PhotoDto> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDto> photos) { this.photos = photos; }

    public AttestationDto getAttestation() { return attestation; }
    public void setAttestation(AttestationDto attestation) { this.attestation = attestation; }

    public List<SortieMaterielDto> getSortiesMateriel() { return sortiesMateriel; }
    public void setSortiesMateriel(List<SortieMaterielDto> sortiesMateriel) { this.sortiesMateriel = sortiesMateriel; }

    public List<RetourMaterielDto> getRetoursMateriel() { return retoursMateriel; }
    public void setRetoursMateriel(List<RetourMaterielDto> retoursMateriel) { this.retoursMateriel = retoursMateriel; }

    public List<ChecklistItemDto> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<ChecklistItemDto> checklistItems) { this.checklistItems = checklistItems; }

    public List<CheckInOutDto> getCheckInOuts() { return checkInOuts; }
    public void setCheckInOuts(List<CheckInOutDto> checkInOuts) { this.checkInOuts = checkInOuts; }
}