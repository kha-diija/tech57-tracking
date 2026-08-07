package com.example.backend.dto.admin.intervention;

import java.time.LocalDateTime;
import java.util.List;

public class InterventionResponse {

    private Integer id;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Double tauxAvancement;
    private Integer numeroVisite;
    private String statut;
    private String localisationGps;
    private Integer missionId;
    private String missionReference;

    // --- NOUVEAU CHAMP AJOUTÉ ---
    private String etablissementDesignation;

    private Integer technicienId;
    private String technicienNom;

    private List<PhotoDto> photos;
    private AttestationDto attestation;

    // --- NOUVEAUX CHAMPS STOCK & CHECKLIST ---
    private List<SortieMaterielDto> sortiesMateriel;
    private List<RetourMaterielDto> retoursMateriel;
    private List<ChecklistItemDto> checklistItems;

    public InterventionResponse() {}

    // Getters et Setters existants...
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
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

    // --- NOUVEAU GETTER ET SETTER ---
    public String getEtablissementDesignation() { return etablissementDesignation; }
    public void setEtablissementDesignation(String etablissementDesignation) { this.etablissementDesignation = etablissementDesignation; }

    public Integer getTechnicienId() { return technicienId; }
    public void setTechnicienId(Integer technicienId) { this.technicienId = technicienId; }
    public String getTechnicienNom() { return technicienNom; }
    public void setTechnicienNom(String technicienNom) { this.technicienNom = technicienNom; }

    public List<PhotoDto> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDto> photos) { this.photos = photos; }
    public AttestationDto getAttestation() { return attestation; }
    public void setAttestation(AttestationDto attestation) { this.attestation = attestation; }

    // --- Getters et Setters pour le Stock ---
    public List<SortieMaterielDto> getSortiesMateriel() { return sortiesMateriel; }
    public void setSortiesMateriel(List<SortieMaterielDto> sortiesMateriel) { this.sortiesMateriel = sortiesMateriel; }

    public List<RetourMaterielDto> getRetoursMateriel() { return retoursMateriel; }
    public void setRetoursMateriel(List<RetourMaterielDto> retoursMateriel) { this.retoursMateriel = retoursMateriel; }

    public List<ChecklistItemDto> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<ChecklistItemDto> checklistItems) { this.checklistItems = checklistItems; }
}