package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CategorieMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Integer idCategorie;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(name = "est_kit", nullable = false)
    private Boolean estKit = false;

    @OneToMany(mappedBy = "categorie")
    private List<Materiel> materiels = new ArrayList<>();
}