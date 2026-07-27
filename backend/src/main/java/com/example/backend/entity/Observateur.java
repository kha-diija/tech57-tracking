package com.example.backend.entity;

import jakarta.persistence.*;

/**
 * Sous-classe `observateur` — correspond au rôle "Client" côté métier
 * (accès lecture seule), selon le schéma v3.
 */
@Entity
@Table(name = "observateur")
@DiscriminatorValue("OBSERVATEUR")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
public class Observateur extends Utilisateur {

    @Column(name = "type_client", length = 50)
    private String typeClient;

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }
}