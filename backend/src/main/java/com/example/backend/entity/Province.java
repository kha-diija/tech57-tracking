package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "province")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Province {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_province")
    private Integer idProvince;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_region", nullable = false)
    private Region region;

    @OneToMany(mappedBy = "province", cascade = CascadeType.ALL)
    private List<Commune> communes = new ArrayList<>();
}