package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "equipe_technique")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EquipeTechnique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_equipe")
    private Integer idEquipe;

    @Column(name = "nom_equipe", nullable = false, length = 100)
    private String nomEquipe;

    @ManyToMany
    @JoinTable(
            name = "equipe_membre",
            joinColumns = @JoinColumn(name = "id_equipe"),
            inverseJoinColumns = @JoinColumn(name = "id_technicien")
    )
    private Set<Technicien> membres;
}