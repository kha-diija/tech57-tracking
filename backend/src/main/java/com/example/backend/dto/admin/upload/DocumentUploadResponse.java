package com.example.backend.dto.admin.upload;

public class DocumentUploadResponse {
    private Integer idSource;
    private String nomFichier;
    private String typeSource;
    private String cheminFichier;
    private String statutIndexation;

    public DocumentUploadResponse(Integer idSource, String nomFichier, String typeSource,
                                  String cheminFichier, String statutIndexation) {
        this.idSource = idSource;
        this.nomFichier = nomFichier;
        this.typeSource = typeSource;
        this.cheminFichier = cheminFichier;
        this.statutIndexation = statutIndexation;
    }

    public Integer getIdSource() { return idSource; }
    public String getNomFichier() { return nomFichier; }
    public String getTypeSource() { return typeSource; }
    public String getCheminFichier() { return cheminFichier; }
    public String getStatutIndexation() { return statutIndexation; }
}