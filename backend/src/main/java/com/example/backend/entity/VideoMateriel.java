package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VideoMateriel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_video")
    private Integer idVideo;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "url_video", nullable = false, length = 500)
    private String urlVideo;

    @Column(nullable = false, length = 30)
    private String fournisseur;

    @Column(name = "id_externe", length = 150)
    private String idExterne;

    @Column(name = "url_miniature", length = 500)
    private String urlMiniature;

    @Column(name = "duree_secondes")
    private Integer dureeSecondes;

    @Column(name = "taille_fichier_mo")
    private Double tailleFichierMo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie")
    private CategorieMateriel categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materiel")
    private Materiel materiel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ajoute_par_admin", nullable = false)
    private Administrateur ajouteParAdmin;

    @Column(name = "date_ajout", nullable = false)
    private LocalDateTime dateAjout = LocalDateTime.now();
}