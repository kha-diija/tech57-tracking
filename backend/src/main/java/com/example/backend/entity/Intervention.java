package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "intervention")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Intervention {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intervention")
    private Integer idIntervention;

    @Column(name = "date_prevue")
    private LocalDateTime datePrevue; // Date planifiée par l'administrateur

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "taux_avancement")
    private Double tauxAvancement = 0.0;

    @Column(name = "numero_visite", nullable = false)
    private Integer numeroVisite = 0;

    @Column(nullable = false, length = 30)
    private String statut = "Planifiée";

    @Column(name = "localisation_gps", length = 100)
    private String localisationGps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission", nullable = false)
    private MissionInstallation mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien")
    private Technicien technicien;

    // --- HISTORIQUE DE TOUTES LES VISITES ---
    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CheckInOut> checkInOuts = new ArrayList<>();

    // --- RELATIONS EXISTANTES ---
    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL)
    private List<SortieMateriel> sortiesMateriel = new ArrayList<>();

    @OneToMany(mappedBy = "intervention", cascade = CascadeType.ALL)
    private List<RetourMateriel> retoursMateriel = new ArrayList<>();
}