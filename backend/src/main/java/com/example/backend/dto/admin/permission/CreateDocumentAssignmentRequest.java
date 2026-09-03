package com.example.backend.dto.admin.permission;

import jakarta.validation.constraints.NotNull;

public class CreateDocumentAssignmentRequest {

    @NotNull(message = "L'ID de l'observateur est obligatoire")
    private Integer idObservateur;

    @NotNull(message = "L'ID du document est obligatoire")
    private Integer idDocument;

    public CreateDocumentAssignmentRequest() {}

    public Integer getIdObservateur() { return idObservateur; }
    public void setIdObservateur(Integer idObservateur) { this.idObservateur = idObservateur; }

    public Integer getIdDocument() { return idDocument; }
    public void setIdDocument(Integer idDocument) { this.idDocument = idDocument; }
}