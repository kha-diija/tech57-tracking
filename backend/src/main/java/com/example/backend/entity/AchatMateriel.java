package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "achat_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AchatMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_achat")
    private Integer idAchat;

    @Column(name = "numero_facture", length = 100)
    private String numeroFacture;

    @Column(length = 150)
    private String fournisseur;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire_ht")
    private Double prixUnitaireHt;

    @Column(name = "date_achat", nullable = false)
    private LocalDateTime dateAchat = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrateur")
    private Administrateur acheteur;
}