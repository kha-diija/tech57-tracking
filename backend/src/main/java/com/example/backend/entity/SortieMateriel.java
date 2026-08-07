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

    // Fusion de demandeur + technicienRecepteur : sortie_materiel n'a
    // qu'une seule colonne id_technicien en base.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien", nullable = false)
    private Technicien technicien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention")
    private Intervention intervention;

    // Validé par un Administrateur OU un GestionnaireStock -> Utilisateur générique
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur")
    private Utilisateur validateur;

    @OneToMany(mappedBy = "sortieMateriel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailSortieMateriel> details = new ArrayList<>();

    // id_materiel / quantite_sortie supprimés : le détail passe désormais
    // par la liste `details` (table detail_sortie_materiel)
}