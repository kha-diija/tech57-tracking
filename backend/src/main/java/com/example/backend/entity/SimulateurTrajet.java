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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement_origine", nullable = false)
    private Etablissement etablissementOrigine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement_destination", nullable = false)
    private Etablissement etablissementDestination;

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