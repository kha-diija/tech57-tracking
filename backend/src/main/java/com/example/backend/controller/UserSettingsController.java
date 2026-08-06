package com.example.backend.controller;

import com.example.backend.dto.settings.ChangePasswordRequest;
import com.example.backend.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/settings")
public class UserSettingsController {

    private final SettingsService settingsService;

    public UserSettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication, // Injecte l'utilisateur connecté sans problème de type
            @Valid @RequestBody ChangePasswordRequest request) {

        // authentication.getName() contient le username / email extrait du Token JWT
        String email = authentication.getName();

        settingsService.changePasswordByEmail(email, request);

        return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été modifié avec succès"));
    }
}