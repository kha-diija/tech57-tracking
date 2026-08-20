package com.example.backend.dto.admin.upload;

public class RessourceUploadResponse {
    private Integer idRessource;
    private String titre;
    private String type;
    private String cheminFichier;

    public RessourceUploadResponse(Integer idRessource, String titre, String type, String cheminFichier) {
        this.idRessource = idRessource;
        this.titre = titre;
        this.type = type;
        this.cheminFichier = cheminFichier;
    }

    public Integer getIdRessource() { return idRessource; }
    public String getTitre() { return titre; }
    public String getType() { return type; }
    public String getCheminFichier() { return cheminFichier; }
}