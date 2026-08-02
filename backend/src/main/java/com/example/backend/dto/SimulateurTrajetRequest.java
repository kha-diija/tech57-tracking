package com.example.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SimulateurTrajetRequest {

    @NotNull
    private Integer idEtablissementOrigine;

    @NotNull
    private Integer idEtablissementDestination;

    @NotNull
    @Pattern(regexp = "Autoroute|Nationale", message = "typeRoute doit être 'Autoroute' ou 'Nationale'")
    private String typeRoute;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double prixCarburantLitre;     // saisi/consulté par l'utilisateur (DH/L)

    // Optionnel : consommation moyenne du véhicule en L/100km.
    // Si absent, une valeur par défaut (véhicule de service) est utilisée côté service.
    private Double consommationL100km;

    // Optionnel : si renseigné, la simulation alimente directement le budget de la mission
    private Integer idMission;

    public Integer getIdEtablissementOrigine() { return idEtablissementOrigine; }
    public void setIdEtablissementOrigine(Integer idEtablissementOrigine) { this.idEtablissementOrigine = idEtablissementOrigine; }

    public Integer getIdEtablissementDestination() { return idEtablissementDestination; }
    public void setIdEtablissementDestination(Integer idEtablissementDestination) { this.idEtablissementDestination = idEtablissementDestination; }

    public String getTypeRoute() { return typeRoute; }
    public void setTypeRoute(String typeRoute) { this.typeRoute = typeRoute; }

    public Double getPrixCarburantLitre() { return prixCarburantLitre; }
    public void setPrixCarburantLitre(Double prixCarburantLitre) { this.prixCarburantLitre = prixCarburantLitre; }

    public Double getConsommationL100km() { return consommationL100km; }
    public void setConsommationL100km(Double consommationL100km) { this.consommationL100km = consommationL100km; }

    public Integer getIdMission() { return idMission; }
    public void setIdMission(Integer idMission) { this.idMission = idMission; }
}