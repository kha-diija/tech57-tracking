package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mission_installation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MissionInstallation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mission")
    private Integer idMission;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 30)
    private String statut = "Planifiée";

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "budget_propose")
    private Double budgetPropose;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement", nullable = false)
    private Etablissement etablissement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrateur", nullable = false)
    private Administrateur administrateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_equipe")
    private EquipeTechnique equipe;

    // sortiesMateriel / retoursMateriel supprimés : pas de colonne
    // id_mission dans sortie_materiel / retour_materiel. Le lien passe
    // uniquement par intervention -> sortiesMateriel / retoursMateriel
    // (voir SortieMaterielRepository.findByIntervention, utilisé dans
    // InterventionService.convertToResponse)
}