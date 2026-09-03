package com.example.backend.dto.technicien.Dashboard;

import java.util.List;

public class AttestationPreviewRequest {
    private String nomSignataire;
    private List<Integer> materielSortiIds;
    private List<Integer> materielRetourIds;
    private List<String> etatsRetours;
    private String checklistJson;

    public String getNomSignataire() { return nomSignataire; }
    public void setNomSignataire(String nomSignataire) { this.nomSignataire = nomSignataire; }

    public List<Integer> getMaterielSortiIds() { return materielSortiIds; }
    public void setMaterielSortiIds(List<Integer> materielSortiIds) { this.materielSortiIds = materielSortiIds; }

    public List<Integer> getMaterielRetourIds() { return materielRetourIds; }
    public void setMaterielRetourIds(List<Integer> materielRetourIds) { this.materielRetourIds = materielRetourIds; }

    public List<String> getEtatsRetours() { return etatsRetours; }
    public void setEtatsRetours(List<String> etatsRetours) { this.etatsRetours = etatsRetours; }

    public String getChecklistJson() { return checklistJson; }
    public void setChecklistJson(String checklistJson) { this.checklistJson = checklistJson; }
}