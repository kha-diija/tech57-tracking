package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordre_mission")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class OrdreMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ordre")
    private Integer idOrdre;

    @Column(name = "numero_ordre", nullable = false, unique = true, length = 50)
    private String numeroOrdre;

    @Column(name = "date_emission", nullable = false)
    private LocalDateTime dateEmission = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission", nullable = false)
    private MissionInstallation mission;
}