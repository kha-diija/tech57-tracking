package com.example.backend.entity;

import jakarta.persistence.*;

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
