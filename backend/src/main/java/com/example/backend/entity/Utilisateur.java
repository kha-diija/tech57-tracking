package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer idUtilisateur;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(name = "mot_de_passe")
    private String motDePasse;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "type_utilisateur", nullable = false, length = 20)
    private String typeUtilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_admin")
    private Utilisateur creeParAdmin;

    @Column(name = "auth_provider", nullable = false, length = 20)
    private String authProvider = "LOCAL";

    @Column(name = "google_id", length = 150, unique = true)
    private String googleId;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "email_verifie", nullable = false)
    private Boolean emailVerifie = false;

    @Column(name = "compte_actif", nullable = false)
    private Boolean compteActif = true;

    @Column(name = "tentatives_echouees", nullable = false)
    private Short tentativesEchouees = 0;

    @Column(name = "compte_verrouille_jusqu")
    private LocalDateTime compteVerrouilleJusqu;

    @Column(name = "is_2fa_active", nullable = false)
    private Boolean is2faActive = false;

    @Column(name = "secret_2fa")
    private String secret2fa;

    @Column(name = "dernier_login")
    private LocalDateTime dernierLogin;

    @Column(name = "derniere_ip", length = 45)
    private String derniereIp;
}