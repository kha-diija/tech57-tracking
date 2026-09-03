package com.example.backend.dto.admin.permission;

import jakarta.validation.constraints.NotNull;

public class CreateResourceAssignmentRequest {

    @NotNull(message = "L'ID de l'observateur est obligatoire")
    private Integer idObservateur;

    @NotNull(message = "L'ID de la ressource est obligatoire")
    private Integer idRessource;

    public CreateResourceAssignmentRequest() {}

    public Integer getIdObservateur() { return idObservateur; }
    public void setIdObservateur(Integer idObservateur) { this.idObservateur = idObservateur; }

    public Integer getIdRessource() { return idRessource; }
    public void setIdRessource(Integer idRessource) { this.idRessource = idRessource; }
}