package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "region")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_region")
    private Integer idRegion;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @OneToMany(mappedBy = "region")
    private List<Province> provinces;
}