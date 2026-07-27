package com.example.backend.entity;

import jakarta.persistence.*;

/**
 * NOUVEAU v3 — Sous-classe `gestionnaire_stock` (4e rôle), correspond au
 * rôle "Gestionnaire" côté métier. Accès restreint au périmètre stock.
 */
@Entity
@Table(name = "gestionnaire_stock")
@DiscriminatorValue("GESTIONNAIRE_STOCK")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
public class GestionnaireStock extends Utilisateur {

    @Column(name = "poste", length = 100)
    private String poste;

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }
}