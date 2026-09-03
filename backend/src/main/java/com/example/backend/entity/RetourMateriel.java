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

    @Column(name = "quantite_rendue", nullable = false)
    private Integer quantite;

    @Column(name = "etat_materiel", nullable = false, length = 30)
    private String etatMateriel = "Bon état"; // Bon état, Endommagé, En panne

    @Column(name = "statut_validation", nullable = false, length = 20)
    private String statut = "En attente"; // En attente, Validée, Rejetée

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_technicien", nullable = false)
    private Technicien technicien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention")
    private Intervention intervention;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur")
    private Utilisateur validateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sortie")
    private SortieMateriel sortieMateriel;

    // missionInstallation supprimé : pas de colonne id_mission dans
    // retour_materiel (le lien passe par intervention -> mission)
}