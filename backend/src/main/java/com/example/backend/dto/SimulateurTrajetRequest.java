package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class SimulateurTrajetRequest {

    @NotNull @Valid
    private PointRequest origine;

    @NotNull @Valid
    private PointRequest destination;

    @NotNull
    @Pattern(regexp = "Autoroute|Nationale", message = "typeRoute doit être 'Autoroute' ou 'Nationale'")
    private String typeRoute;

    @NotNull
    private Double prixCarburantLitre;     // saisi/consulté par l'utilisateur (DH/L)

    // Optionnel : consommation moyenne du véhicule en L/100km (défaut appliqué côté service)
    private Double consommationL100km;

    // Optionnel : si renseigné, la simulation alimente directement le budget de la mission
    private Integer idMission;

    public PointRequest getOrigine() { return origine; }
    public void setOrigine(PointRequest origine) { this.origine = origine; }

    public PointRequest getDestination() { return destination; }
    public void setDestination(PointRequest destination) { this.destination = destination; }

    public String getTypeRoute() { return typeRoute; }
    public void setTypeRoute(String typeRoute) { this.typeRoute = typeRoute; }

    public Double getPrixCarburantLitre() { return prixCarburantLitre; }
    public void setPrixCarburantLitre(Double prixCarburantLitre) { this.prixCarburantLitre = prixCarburantLitre; }

    public Double getConsommationL100km() { return consommationL100km; }
    public void setConsommationL100km(Double consommationL100km) { this.consommationL100km = consommationL100km; }

    public Integer getIdMission() { return idMission; }
    public void setIdMission(Integer idMission) { this.idMission = idMission; }

    /**
     * Un point de trajet = SOIT un établissement existant (idEtablissement renseigné),
     * SOIT un lieu libre trouvé via la recherche d'adresse (nom + lat + lng renseignés,
     * typiquement le résultat choisi dans l'autocomplete Nominatim côté frontend).
     */
    public static class PointRequest {
        private Integer idEtablissement;
        private String nom;
        private Double lat;
        private Double lng;

        public Integer getIdEtablissement() { return idEtablissement; }
        public void setIdEtablissement(Integer idEtablissement) { this.idEtablissement = idEtablissement; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }

        public Double getLat() { return lat; }
        public void setLat(Double lat) { this.lat = lat; }

        public Double getLng() { return lng; }
        public void setLng(Double lng) { this.lng = lng; }

        public boolean estLibre() { return idEtablissement == null; }
    }
}