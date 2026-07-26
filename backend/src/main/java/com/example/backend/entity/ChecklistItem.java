package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "checklist_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChecklistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Integer idItem;

    @Column(nullable = false)
    private Integer quantite = 1;

    @Column(name = "etat_constate", length = 100)
    private String etatConstate;

    @Column(nullable = false)
    private Boolean conforme = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_checklist", nullable = false)
    private ChecklistEquipement checklist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel", nullable = false)
    private Materiel materiel;
}