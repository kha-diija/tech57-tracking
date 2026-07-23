package com.example.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Classe abstraite `utilisateur` — héritage par table (JOINED),
 * discriminant = colonne `type_utilisateur` déjà présente en base
 * (ADMINISTRATEUR / TECHNICIEN / OBSERVATEUR / GESTIONNAIRE_STOCK).
 */
@Entity
@Table(name = "utilisateur")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type_utilisateur", discriminatorType = DiscriminatorType.STRING, length = 20)
public abstract class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Integer id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "telephone", length = 30)
    private String telephone;

    /** Toujours haché (BCrypt). Nullable si auth_provider = GOOGLE. */
    @Column(name = "mot_de_passe", length = 255)
    private String motDePasse;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_role")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_admin")
    private Utilisateur creeParAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "google_id", unique = true, length = 150)
    private String googleId;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column(name = "email_verifie", nullable = false)
    private boolean emailVerifie = false;

    @Column(name = "compte_actif", nullable = false)
    private boolean compteActif = true;

    @Column(name = "tentatives_echouees", nullable = false)
    private Short tentativesEchouees = 0;

    @Column(name = "compte_verrouille_jusqu")
    private LocalDateTime compteVerrouilleJusqu;

    @Column(name = "is_2fa_active", nullable = false)
    private boolean is2faActive = false;

    @Column(name = "secret_2fa", length = 255)
    private String secret2fa;

    @Column(name = "dernier_login")
    private LocalDateTime dernierLogin;

    @Column(name = "derniere_ip", length = 45)
    private String derniereIp;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }

    /**
     * Renvoie le discriminant JPA (ADMINISTRATEUR / TECHNICIEN / OBSERVATEUR /
     * GESTIONNAIRE_STOCK) à partir de l'annotation @DiscriminatorValue de la
     * sous-classe concrète. Utile pour construire les rôles Spring Security
     * (ROLE_xxx) et le JWT sans dupliquer la colonne en tant que champ mappé.
     */
    @Transient
    public String getTypeUtilisateur() {
        DiscriminatorValue dv = this.getClass().getAnnotation(DiscriminatorValue.class);
        return dv != null ? dv.value() : null;
    }

    // ----- Getters / Setters -----

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Utilisateur getCreeParAdmin() {
        return creeParAdmin;
    }

    public void setCreeParAdmin(Utilisateur creeParAdmin) {
        this.creeParAdmin = creeParAdmin;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isEmailVerifie() {
        return emailVerifie;
    }

    public void setEmailVerifie(boolean emailVerifie) {
        this.emailVerifie = emailVerifie;
    }

    public boolean isCompteActif() {
        return compteActif;
    }

    public void setCompteActif(boolean compteActif) {
        this.compteActif = compteActif;
    }

    public Short getTentativesEchouees() {
        return tentativesEchouees;
    }

    public void setTentativesEchouees(Short tentativesEchouees) {
        this.tentativesEchouees = tentativesEchouees;
    }

    public LocalDateTime getCompteVerrouilleJusqu() {
        return compteVerrouilleJusqu;
    }

    public void setCompteVerrouilleJusqu(LocalDateTime compteVerrouilleJusqu) {
        this.compteVerrouilleJusqu = compteVerrouilleJusqu;
    }

    public boolean isIs2faActive() {
        return is2faActive;
    }

    public void setIs2faActive(boolean is2faActive) {
        this.is2faActive = is2faActive;
    }

    public String getSecret2fa() {
        return secret2fa;
    }

    public void setSecret2fa(String secret2fa) {
        this.secret2fa = secret2fa;
    }

    public LocalDateTime getDernierLogin() {
        return dernierLogin;
    }

    public void setDernierLogin(LocalDateTime dernierLogin) {
        this.dernierLogin = dernierLogin;
    }

    public String getDerniereIp() {
        return derniereIp;
    }

    public void setDerniereIp(String derniereIp) {
        this.derniereIp = derniereIp;
    }
}
