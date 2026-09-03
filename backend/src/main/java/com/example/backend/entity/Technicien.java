package com.example.backend.entity;

import jakarta.persistence.*;
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

    // --- RELATIONS v4 (fusion de sortiesRecues + sortiesDemandees) ---

    @OneToMany(mappedBy = "technicien")
    private List<SortieMateriel> sortiesMateriel = new ArrayList<>();

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

    public List<SortieMateriel> getSortiesMateriel() {
        return sortiesMateriel;
    }

    public void setSortiesMateriel(List<SortieMateriel> sortiesMateriel) {
        this.sortiesMateriel = sortiesMateriel;
    }

    public List<RetourMateriel> getRetoursEffectues() {
        return retoursEffectues;
    }

    public void setRetoursEffectues(List<RetourMateriel> retoursEffectues) {
        this.retoursEffectues = retoursEffectues;
    }
}