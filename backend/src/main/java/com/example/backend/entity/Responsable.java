package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "responsable")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Responsable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_responsable")
    private Integer idResponsable;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 100)
    private String fonction;

    @Column(length = 30)
    private String telephone;

    @Column(length = 150)
    private String email;
}