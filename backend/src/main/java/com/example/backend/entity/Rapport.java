package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rapport")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Rapport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rapport")
    private Integer idRapport;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 20)
    private String format = "PDF";

    @Column(name = "date_generation", nullable = false)
    private LocalDateTime dateGeneration = LocalDateTime.now();

    @Column(name = "genere_par_ia", nullable = false)
    private Boolean genereParIa = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention")
    private Intervention intervention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission")
    private MissionInstallation mission;
}