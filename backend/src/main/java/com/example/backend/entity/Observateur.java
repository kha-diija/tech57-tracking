package com.example.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "observateur")
@DiscriminatorValue("OBSERVATEUR")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
public class Observateur extends Utilisateur {

    @Column(name = "type_client", length = 50)
    private String typeClient;

    @Column(name = "adresse", length = 255)
    private String adresse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement")
    private Etablissement etablissement;

    public String getTypeClient() {
        return typeClient;
    }

    public void setTypeClient(String typeClient) {
        this.typeClient = typeClient;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Etablissement getEtablissement() {
        return etablissement;
    }

    public void setEtablissement(Etablissement etablissement) {
        this.etablissement = etablissement;
    }
}