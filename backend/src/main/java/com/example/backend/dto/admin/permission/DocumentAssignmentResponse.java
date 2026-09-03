package com.example.backend.dto.admin.permission;

import com.example.backend.dto.admin.ObservateurSummaryDto;
import java.time.LocalDateTime;

public class DocumentAssignmentResponse {

    private Long id;
    private ObservateurSummaryDto observateur;
    private Integer idDocument;
    private String nomFichier;
    private String typeDocument;
    private LocalDateTime dateAssignation;
    private Boolean actif;
    private String assigneParAdminNom;

    public DocumentAssignmentResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ObservateurSummaryDto getObservateur() { return observateur; }
    public void setObservateur(ObservateurSummaryDto observateur) { this.observateur = observateur; }

    public Integer getIdDocument() { return idDocument; }
    public void setIdDocument(Integer idDocument) { this.idDocument = idDocument; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public LocalDateTime getDateAssignation() { return dateAssignation; }
    public void setDateAssignation(LocalDateTime dateAssignation) { this.dateAssignation = dateAssignation; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getAssigneParAdminNom() { return assigneParAdminNom; }
    public void setAssigneParAdminNom(String assigneParAdminNom) { this.assigneParAdminNom = assigneParAdminNom; }
}