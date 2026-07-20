package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technicien")
@PrimaryKeyJoinColumn(name = "id_utilisateur")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Technicien extends Utilisateur {

    @Column(length = 100)
    private String vehicule;

    @Column(length = 50)
    private String matricule;
}