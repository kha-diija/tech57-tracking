package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Kpi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_kpi")
    private Integer idKpi;

    @Column(nullable = false, length = 150)
    private String nom;

    private Double valeur;

    @Column(length = 20)
    private String unite;

    @Column(name = "date_calcul", nullable = false)
    private LocalDateTime dateCalcul = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tableau")
    private TableauBord tableauBord;
}