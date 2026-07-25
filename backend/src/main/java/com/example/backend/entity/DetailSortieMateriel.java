package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detail_sortie_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DetailSortieMateriel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail_sortie")
    private Integer idDetailSortie;

    @Column(nullable = false)
    private Integer quantite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sortie", nullable = false)
    private SortieMateriel sortieMateriel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;
}