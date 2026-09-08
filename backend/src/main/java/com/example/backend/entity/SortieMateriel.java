package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sortie_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SortieMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sortie")
    private Integer idSortie;

    @Column(name = "date_sortie", nullable = false)
    private LocalDateTime dateSortie = LocalDateTime.now();

    @Column(name = "lieu_intervention", length = 200)
    private String lieuIntervention;

    @Column(name = "statut_validation", nullable = false, length = 20)
    private String statut = "En attente"; // En attente, Validée, Rejetée

    @Column(name = "motif_rejet", columnDefinition = "TEXT")
    private String motif;

    @Column(name = "retour_traite", nullable = false)
    private Boolean retourTraite = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien", nullable = false)
    private Technicien technicien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention")
    private Intervention intervention;

    // ✅ NOUVEAU : Lien avec la mission (pour les missions proposées)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission", nullable = true)
    private MissionInstallation mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur")
    private Utilisateur validateur;

    @OneToMany(mappedBy = "sortieMateriel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailSortieMateriel> details = new ArrayList<>();
}