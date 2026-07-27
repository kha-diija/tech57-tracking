package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "equipe_membre",
            joinColumns = @JoinColumn(name = "id_equipe"),
            inverseJoinColumns = @JoinColumn(name = "id_technicien")
    )
    private List<Technicien> membres = new ArrayList<>();

    @OneToMany(mappedBy = "equipe")
    private List<MissionInstallation> missions = new ArrayList<>();
}