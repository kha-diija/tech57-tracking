package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attestation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Attestation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_attestation")
    private Integer idAttestation;

    @Column(name = "date_signature")
    private LocalDateTime dateSignature;

    @Column(name = "signature_numerique", columnDefinition = "TEXT")
    private String signatureNumerique;

    @Column(name = "nom_signataire", length = 150)
    private String nomSignataire;

    @Column(nullable = false)
    private Boolean valide = false;

    @Column(name = "chemin_fichier", length = 255)
    private String cheminFichier;

    // ✅ NOUVEAUX CHAMPS (AJOUTER CES 4 LIGNES)
    @Column(name = "statut", length = 30)
    private String statut = "GENEREE";

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @Column(name = "chemin_fichier_signe", length = 255)
    private String cheminFichierSigne;

    @Column(name = "date_upload_signe")
    private LocalDateTime dateUploadSigne;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention", nullable = false, unique = true)
    private Intervention intervention;
}