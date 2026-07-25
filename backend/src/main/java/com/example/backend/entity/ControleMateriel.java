package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "controle_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ControleMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_controle")
    private Integer idControle;

    @Column(name = "date_controle", nullable = false)
    private LocalDateTime dateControle = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    private String resultat = "Conforme"; // Conforme, Anomalie, À réparer

    @Column(columnDefinition = "TEXT")
    private String remarques;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_controleur", nullable = false)
    private Utilisateur controleur;
}