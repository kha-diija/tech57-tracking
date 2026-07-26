package com.example.backend.security;

import com.example.backend.entity.Utilisateur;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Integer id;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String motDePasse;
    private final String typeUtilisateur;
    private final boolean compteActif;
    private final LocalDateTime compteVerrouilleJusqu;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Integer id, String nom, String prenom, String email, String motDePasse,
                          String typeUtilisateur, boolean compteActif,
                          LocalDateTime compteVerrouilleJusqu,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.typeUtilisateur = typeUtilisateur;
        this.compteActif = compteActif;
        this.compteVerrouilleJusqu = compteVerrouilleJusqu;
        this.authorities = authorities;
    }

    public static UserPrincipal create(Utilisateur utilisateur) {
        String role = "ROLE_" + utilisateur.getTypeUtilisateur();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        return new UserPrincipal(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getMotDePasse(),
                utilisateur.getTypeUtilisateur(),
                utilisateur.isCompteActif(),
                utilisateur.getCompteVerrouilleJusqu(),
                authorities
        );
    }

    public Integer getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getTypeUtilisateur() {
        return typeUtilisateur;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return motDePasse;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return compteVerrouilleJusqu == null || compteVerrouilleJusqu.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return compteActif;
    }
}
