package com.example.backend.dto.technicien.Dashboard;

import java.time.LocalDateTime;
import java.util.List;
import com.example.backend.dto.admin.Mission.MissionMaterielDTO;

public class MissionSimplifieeDTO {
    private Integer idMission;
    private String reference;
    private String titre;
    private String statut;
    private LocalDateTime dateCreation;
    private Double budgetPropose;
    private Integer etablissementId;
    private String etablissementDesignation;

    // ✅ NOUVEAU
    private List<MissionMaterielDTO> materiels;

    // Getters et Setters
    public Integer getIdMission() { return idMission; }
    public void setIdMission(Integer idMission) { this.idMission = idMission; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Double getBudgetPropose() { return budgetPropose; }
    public void setBudgetPropose(Double budgetPropose) { this.budgetPropose = budgetPropose; }

    public Integer getEtablissementId() { return etablissementId; }
    public void setEtablissementId(Integer etablissementId) { this.etablissementId = etablissementId; }

    public String getEtablissementDesignation() { return etablissementDesignation; }
    public void setEtablissementDesignation(String etablissementDesignation) { this.etablissementDesignation = etablissementDesignation; }

    public List<MissionMaterielDTO> getMateriels() { return materiels; }
    public void setMateriels(List<MissionMaterielDTO> materiels) { this.materiels = materiels; }
}