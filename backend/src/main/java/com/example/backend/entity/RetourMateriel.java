package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "retour_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RetourMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_retour")
    private Integer idRetour;

    @Column(name = "date_retour", nullable = false)
    private LocalDateTime dateRetour = LocalDateTime.now();

    @Column(nullable = false)
    private Integer quantite;

    @Column(nullable = false, length = 30)
    private String etatMateriel = "Bon état"; // Bon état, Endommagé, HS

    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien", nullable = false)
    private Technicien technicien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission")
    private MissionInstallation missionInstallation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention")
    private Intervention intervention;
}