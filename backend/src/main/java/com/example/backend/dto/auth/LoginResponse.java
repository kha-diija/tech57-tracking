package com.example.backend.dto.auth;

public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    /** ADMINISTRATEUR / TECHNICIEN / OBSERVATEUR :partenaire et formateur/ GESTIONNAIRE_STOCK */
    private String role;
    /** Route Angular vers laquelle rediriger selon le rôle. */
    private String redirectUrl;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, Integer id, String nom,
                          String prenom, String email, String role, String redirectUrl) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.redirectUrl = redirectUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
