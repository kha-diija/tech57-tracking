package com.example.backend.dto.admin.permission;

import com.example.backend.dto.admin.ObservateurSummaryDto;
import java.time.LocalDateTime;

public class ResourceAssignmentResponse {

    private Long id;
    private ObservateurSummaryDto observateur;
    private Integer idRessource;
    private String titreRessource;
    private String typeRessource;
    private LocalDateTime dateAssignation;
    private Boolean actif;
    private String assigneParAdminNom;

    public ResourceAssignmentResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ObservateurSummaryDto getObservateur() { return observateur; }
    public void setObservateur(ObservateurSummaryDto observateur) { this.observateur = observateur; }

    public Integer getIdRessource() { return idRessource; }
    public void setIdRessource(Integer idRessource) { this.idRessource = idRessource; }

    public String getTitreRessource() { return titreRessource; }
    public void setTitreRessource(String titreRessource) { this.titreRessource = titreRessource; }

    public String getTypeRessource() { return typeRessource; }
    public void setTypeRessource(String typeRessource) { this.typeRessource = typeRessource; }

    public LocalDateTime getDateAssignation() { return dateAssignation; }
    public void setDateAssignation(LocalDateTime dateAssignation) { this.dateAssignation = dateAssignation; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getAssigneParAdminNom() { return assigneParAdminNom; }
    public void setAssigneParAdminNom(String assigneParAdminNom) { this.assigneParAdminNom = assigneParAdminNom; }
}