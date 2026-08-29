package com.example.backend.entity;

import jakarta.persistence.*;

/**
 * Rôle PARTENAIRE — rattaché à une Province.
 * Voit uniquement les données (établissements, avancement) de sa province.
 */
@Entity
@Table(name = "partenaire")
@DiscriminatorValue("PARTENAIRE")
@PrimaryKeyJoinColumn(name = "id_utilisateur") // FK vers utilisateur.id_utilisateur
public class Partenaire extends Utilisateur {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_province", nullable = false)
    private Province province;

    @Column(name = "organisation", length = 150)
    private String organisation;

    @Column(name = "fonction", length = 100)
    private String fonction;

    public Partenaire() {
    }

    public Province getProvince() {
        return province;
    }

    public void setProvince(Province province) {
        this.province = province;
    }

    public String getOrganisation() {
        return organisation;
    }

    public void setOrganisation(String organisation) {
        this.organisation = organisation;
    }

    public String getFonction() {
        return fonction;
    }

    public void setFonction(String fonction) {
        this.fonction = fonction;
    }
}