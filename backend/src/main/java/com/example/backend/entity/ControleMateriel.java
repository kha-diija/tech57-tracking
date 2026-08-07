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

    // --- Rapprochement stock (sortie / installée / rendue) ---
    @Column(name = "quantite_sortie")
    private Integer quantiteSortie;

    @Column(name = "quantite_installee")
    private Integer quantiteInstallee;

    @Column(name = "quantite_rendue")
    private Integer quantiteRendue;

    @Column(name = "ecart_constate")
    private Integer ecartConstate;

    @Column(nullable = false)
    private Boolean conforme = true;

    // --- Contrôle qualité ---
    @Column(nullable = false, length = 30)
    private String resultat = "Conforme"; // Conforme, Anomalie, À réparer

    @Column(columnDefinition = "TEXT")
    private String remarques;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gestionnaire", nullable = false)
    private GestionnaireStock gestionnaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_controleur", nullable = false)
    private Utilisateur controleur;
}