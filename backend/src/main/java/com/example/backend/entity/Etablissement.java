package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etablissement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Etablissement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etablissement")
    private Integer idEtablissement;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "localisation_gps", length = 100)
    private String localisationGps;

    @Column(name = "nombre_beneficiaires")
    private Integer nombreBeneficiaires;

    @Column(name = "nombre_beneficiaires_reel")
    private Integer nombreBeneficiairesReel;

    @Column(name = "telephone_contact", length = 30)
    private String telephoneContact;

    @Column(name = "email_contact", length = 150)
    private String emailContact;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_commune", nullable = false)
    private Commune commune;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private Responsable responsable;

    @OneToMany(mappedBy = "etablissement", cascade = CascadeType.ALL)
    private List<Materiel> materiels = new ArrayList<>();

    @OneToMany(mappedBy = "etablissement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Observateur> observateurs = new ArrayList<>();
}