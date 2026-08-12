package com.example.backend.dto.admin.permission;

import com.example.backend.dto.admin.ObservateurSummaryDto;
import java.time.LocalDateTime;

public class VideoAssignmentResponse {

    private Long id;
    private ObservateurSummaryDto observateur;
    private Integer idVideo;
    private String titreVideo;
    private LocalDateTime dateAssignation;
    private Boolean actif;
    private String assigneParAdminNom;

    public VideoAssignmentResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ObservateurSummaryDto getObservateur() { return observateur; }
    public void setObservateur(ObservateurSummaryDto observateur) { this.observateur = observateur; }

    public Integer getIdVideo() { return idVideo; }
    public void setIdVideo(Integer idVideo) { this.idVideo = idVideo; }

    public String getTitreVideo() { return titreVideo; }
    public void setTitreVideo(String titreVideo) { this.titreVideo = titreVideo; }

    public LocalDateTime getDateAssignation() { return dateAssignation; }
    public void setDateAssignation(LocalDateTime dateAssignation) { this.dateAssignation = dateAssignation; }

    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }

    public String getAssigneParAdminNom() { return assigneParAdminNom; }
    public void setAssigneParAdminNom(String assigneParAdminNom) { this.assigneParAdminNom = assigneParAdminNom; }
}