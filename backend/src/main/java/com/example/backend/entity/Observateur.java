package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "observateur")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Observateur extends Utilisateur {

    @Column(name = "type_client", length = 50)
    private String typeClient;

    @ManyToMany
    @JoinTable(
            name = "etablissement_observateur",
            joinColumns = @JoinColumn(name = "id_observateur"),
            inverseJoinColumns = @JoinColumn(name = "id_etablissement")
    )
    private Set<Etablissement> etablissementsRattaches;
}