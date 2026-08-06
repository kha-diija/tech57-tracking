package com.example.backend.service;

import com.example.backend.dto.settings.ChangePasswordRequest;
import com.example.backend.entity.Utilisateur;
import com.example.backend.exception.InvalidCredentialsException;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public SettingsService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("Utilisateur introuvable"));

        // Vérification de l'ancien mot de passe
        if (utilisateur.getMotDePasse() == null ||
                !passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            throw new InvalidCredentialsException("L'ancien mot de passe est incorrect");
        }

        // Le nouveau mot de passe doit être différent de l'ancien
        if (passwordEncoder.matches(request.getNouveauMotDePasse(), utilisateur.getMotDePasse())) {
            throw new InvalidCredentialsException("Le nouveau mot de passe doit être différent de l'ancien mot de passe");
        }

        // Mise à jour du mot de passe
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }
}