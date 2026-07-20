package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Materiel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_materiel")
    private Integer idMateriel;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(name = "numero_serie", unique = true, length = 100)
    private String numeroSerie;

    @Column(name = "code_qr", unique = true, length = 150)
    private String codeQr;

    @Column(nullable = false, length = 30)
    private String etat = "Neuf";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie")
    private CategorieMateriel categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement")
    private Etablissement etablissement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel_parent")
    private Materiel materielParent;

    @OneToMany(mappedBy = "materielParent", cascade = CascadeType.ALL)
    private List<Materiel> composants;

    @Column(name = "quantite_composant")
    private Integer quantiteComposant = 1;
}