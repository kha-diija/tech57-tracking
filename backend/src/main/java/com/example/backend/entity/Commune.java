package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commune")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Commune {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_commune")
    private Integer idCommune;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    @OneToMany(mappedBy = "commune", cascade = CascadeType.ALL)
    private List<Etablissement> etablissements = new ArrayList<>();
}