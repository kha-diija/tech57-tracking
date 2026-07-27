package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Sous-classe `technicien`.
 */
@Entity
@Table(name = "technicien")
@DiscriminatorValue("TECHNICIEN")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
public class Technicien extends Utilisateur {

    @Column(name = "vehicule", length = 100)
    private String vehicule;

    @Column(name = "matricule", length = 50)
    private String matricule;

    // --- NOUVELLES RELATIONS v3.0 ---

    @OneToMany(mappedBy = "technicienRecepteur")
    private List<SortieMateriel> sortiesRecues = new ArrayList<>();

    @OneToMany(mappedBy = "demandeur")
    private List<SortieMateriel> sortiesDemandees = new ArrayList<>();

    @OneToMany(mappedBy = "technicien")
    private List<RetourMateriel> retoursEffectues = new ArrayList<>();

    public String getVehicule() {
        return vehicule;
    }

    public void setVehicule(String vehicule) {
        this.vehicule = vehicule;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
}