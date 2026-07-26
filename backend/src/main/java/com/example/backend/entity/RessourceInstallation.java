package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ressource_installation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RessourceInstallation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ressource")
    private Integer idRessource;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(name = "chemin_fichier", nullable = false, length = 255)
    private String cheminFichier;

    @Column(name = "valide_par_admin", nullable = false)
    private Boolean valideParAdmin = false;

    @Column(name = "date_ajout", nullable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_etablissement", nullable = false)
    private Etablissement etablissement;
}