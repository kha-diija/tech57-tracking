package com.example.backend.dto.admin.intervention;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UpdateInterventionRequest {

    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    @PositiveOrZero(message = "Le taux d'avancement doit être supérieur ou égal à 0")
    private Double tauxAvancement;

    @NotNull(message = "Le numéro de visite est obligatoire")
    private Integer numeroVisite;

    @NotNull(message = "Le statut est obligatoire")
    @Size(max = 30)
    private String statut;

    @Size(max = 100)
    private String localisationGps;

    @NotNull(message = "La mission est obligatoire")
    private Integer missionId;

    @NotNull(message = "Le technicien est obligatoire")
    private Integer technicienId;

    public UpdateInterventionRequest() {
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public Double getTauxAvancement() {
        return tauxAvancement;
    }

    public void setTauxAvancement(Double tauxAvancement) {
        this.tauxAvancement = tauxAvancement;
    }

    public Integer getNumeroVisite() {
        return numeroVisite;
    }

    public void setNumeroVisite(Integer numeroVisite) {
        this.numeroVisite = numeroVisite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getLocalisationGps() {
        return localisationGps;
    }

    public void setLocalisationGps(String localisationGps) {
        this.localisationGps = localisationGps;
    }

    public Integer getMissionId() {
        return missionId;
    }

    public void setMissionId(Integer missionId) {
        this.missionId = missionId;
    }

    public Integer getTechnicienId() {
        return technicienId;
    }

    public void setTechnicienId(Integer technicienId) {
        this.technicienId = technicienId;
    }
}