package com.example.backend.repository;

import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUtilisateurAndRevokedFalse(Utilisateur utilisateur);

    void deleteByUtilisateur(Utilisateur utilisateur);
}
