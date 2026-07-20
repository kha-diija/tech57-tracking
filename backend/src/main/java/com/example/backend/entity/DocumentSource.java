package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_source")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DocumentSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_source")
    private Integer idSource;

    @Column(name = "nom_fichier", nullable = false, length = 255)
    private String nomFichier;

    @Column(name = "type_source", nullable = false, length = 20)
    private String typeSource;

    @Column(name = "chemin_fichier", length = 255)
    private String cheminFichier;

    @Column(name = "date_import", nullable = false)
    private LocalDateTime dateImport = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_uploader")
    private Administrateur uploader;

    @Column(name = "statut_indexation", nullable = false, length = 20)
    private String statutIndexation = "EN_ATTENTE";
}