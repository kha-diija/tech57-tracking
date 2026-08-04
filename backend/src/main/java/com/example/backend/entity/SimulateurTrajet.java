package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "simulateur_trajet")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SimulateurTrajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulation")
    private Integer idSimulation;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "type_route", length = 20)
    private String typeRoute;

    @Column(name = "cout_gasoil")
    private Double coutGasoil;

    @Column(name = "cout_peage")
    private Double coutPeage;

    @Column(name = "temps_estime")
    private Double tempsEstime;

    @Column(name = "cout_total")
    private Double coutTotal;

    // --- Origine : établissement OU point libre ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement_origine")
    private Etablissement etablissementOrigine;

    @Column(name = "nom_origine")
    private String nomOrigine;

    @Column(name = "lat_origine")
    private Double latOrigine;

    @Column(name = "lng_origine")
    private Double lngOrigine;

    // --- Destination : établissement OU point libre ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement_destination")
    private Etablissement etablissementDestination;

    @Column(name = "nom_destination")
    private String nomDestination;

    @Column(name = "lat_destination")
    private Double latDestination;

    @Column(name = "lng_destination")
    private Double lngDestination;

    // --- Auteur ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien")
    private Technicien technicien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrateur")
    private Administrateur administrateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission")
    private MissionInstallation mission;
}