package com.example.backend.dto;

import java.util.List;

/**
 * Réponse du simulateur de trajet (calcul "type Waze").
 * `pointsRoute` n'est PAS persisté en base (la table simulateur_trajet ne
 * stocke que le résumé chiffré) : c'est la géométrie du trajet, renvoyée
 * uniquement pour tracer la polyline sur la carte Leaflet côté frontend.
 */
public class SimulateurTrajetDTO {

    private Integer idSimulation;

    private String referenceOrigine;
    private String designationOrigine;
    private double latOrigine;
    private double lngOrigine;

    private String referenceDestination;
    private String designationDestination;
    private double latDestination;
    private double lngDestination;

    private String typeRoute;              // Autoroute / Nationale
    private Double distanceKm;
    private Double tempsEstime;        // heures
    private Double coutGasoil;
    private Double coutPeage;
    private Double coutTotal;

    private Integer idMission;             // renseigné si le budget a été proposé à une mission

    // [lat, lng] successifs du tracé réel renvoyé par le moteur de routage
    private List<double[]> pointsRoute;

    public SimulateurTrajetDTO() {}

    // --- Getters / Setters ---

    public Integer getIdSimulation() { return idSimulation; }
    public void setIdSimulation(Integer idSimulation) { this.idSimulation = idSimulation; }

    public String getReferenceOrigine() { return referenceOrigine; }
    public void setReferenceOrigine(String referenceOrigine) { this.referenceOrigine = referenceOrigine; }

    public String getDesignationOrigine() { return designationOrigine; }
    public void setDesignationOrigine(String designationOrigine) { this.designationOrigine = designationOrigine; }

    public double getLatOrigine() { return latOrigine; }
    public void setLatOrigine(double latOrigine) { this.latOrigine = latOrigine; }

    public double getLngOrigine() { return lngOrigine; }
    public void setLngOrigine(double lngOrigine) { this.lngOrigine = lngOrigine; }

    public String getReferenceDestination() { return referenceDestination; }
    public void setReferenceDestination(String referenceDestination) { this.referenceDestination = referenceDestination; }

    public String getDesignationDestination() { return designationDestination; }
    public void setDesignationDestination(String designationDestination) { this.designationDestination = designationDestination; }

    public double getLatDestination() { return latDestination; }
    public void setLatDestination(double latDestination) { this.latDestination = latDestination; }

    public double getLngDestination() { return lngDestination; }
    public void setLngDestination(double lngDestination) { this.lngDestination = lngDestination; }

    public String getTypeRoute() { return typeRoute; }
    public void setTypeRoute(String typeRoute) { this.typeRoute = typeRoute; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Double getTempsEstime() { return tempsEstime; }
    public void setTempsEstime(Double tempsEstime) { this.tempsEstime = tempsEstime; }

    public Double getCoutGasoil() { return coutGasoil; }
    public void setCoutGasoil(Double coutGasoil) { this.coutGasoil = coutGasoil; }

    public Double getCoutPeage() { return coutPeage; }
    public void setCoutPeage(Double coutPeage) { this.coutPeage = coutPeage; }

    public Double getCoutTotal() { return coutTotal; }
    public void setCoutTotal(Double coutTotal) { this.coutTotal = coutTotal; }

    public Integer getIdMission() { return idMission; }
    public void setIdMission(Integer idMission) { this.idMission = idMission; }

    public List<double[]> getPointsRoute() { return pointsRoute; }
    public void setPointsRoute(List<double[]> pointsRoute) { this.pointsRoute = pointsRoute; }
}