package com.example.backend.dto.admin.permission;

import jakarta.validation.constraints.NotNull;

public class CreateVideoAssignmentRequest {

    @NotNull(message = "L'ID de l'observateur est obligatoire")
    private Integer idObservateur;

    @NotNull(message = "L'ID de la vidéo est obligatoire")
    private Integer idVideo;

    public CreateVideoAssignmentRequest() {}

    public Integer getIdObservateur() { return idObservateur; }
    public void setIdObservateur(Integer idObservateur) { this.idObservateur = idObservateur; }

    public Integer getIdVideo() { return idVideo; }
    public void setIdVideo(Integer idVideo) { this.idVideo = idVideo; }
}