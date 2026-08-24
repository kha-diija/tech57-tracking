package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MissionMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mission_materiel")
    private Integer idMissionMateriel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission", nullable = false)
    private MissionInstallation mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;

    @Column(name = "quantite", nullable = false)
    private Integer quantite = 1;

    @Column(name = "statut", nullable = false, length = 20)
    private String statut = "PROPOSE"; // PROPOSE / APPROUVE / REJETE

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;
}