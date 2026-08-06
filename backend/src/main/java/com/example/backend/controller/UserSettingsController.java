package com.example.backend.controller;

import com.example.backend.dto.settings.ChangePasswordRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {

        settingsService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Votre mot de passe a été modifié avec succès"));
    }
}