package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "technicien")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Technicien extends Utilisateur {

    @Column(length = 100)
    private String vehicule;

    @Column(length = 50)
    private String matricule;

    // --- NOUVELLES RELATIONS v3.0 ---

    @OneToMany(mappedBy = "technicienRecepteur")
    private List<SortieMateriel> sortiesRecues = new ArrayList<>();

    @OneToMany(mappedBy = "demandeur")
    private List<SortieMateriel> sortiesDemandees = new ArrayList<>();

    @OneToMany(mappedBy = "technicien")
    private List<RetourMateriel> retoursEffectues = new ArrayList<>();
}