package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
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
    private List<Materiel> composants = new ArrayList<>();

    @Column(name = "quantite_composant")
    private Integer quantiteComposant = 1;

    // --- NOUVELLES RELATIONS v3.0 ---

    @OneToMany(mappedBy = "materiel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetailSortieMateriel> detailsSortie = new ArrayList<>();

    @OneToMany(mappedBy = "materiel", cascade = CascadeType.ALL)
    private List<RetourMateriel> retours = new ArrayList<>();

    @OneToMany(mappedBy = "materiel", cascade = CascadeType.ALL)
    private List<ControleMateriel> controles = new ArrayList<>();
    @OneToMany(mappedBy = "materiel", cascade = CascadeType.ALL)
    private List<MissionMateriel> missions = new ArrayList<>();
}