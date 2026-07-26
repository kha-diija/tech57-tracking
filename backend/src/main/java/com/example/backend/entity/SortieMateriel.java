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

    @Column(nullable = false, length = 30)
    private String statut = "En attente"; // En attente, Validée, Annulée

    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_demandeur")
    private Technicien demandeur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien_recepteur", nullable = false)
    private Technicien technicienRecepteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mission")
    private MissionInstallation missionInstallation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention")
    private Intervention intervention;

    @OneToMany(mappedBy = "sortieMateriel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailSortieMateriel> details = new ArrayList<>();
}