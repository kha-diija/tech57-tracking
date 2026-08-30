package com.example.backend.controller;

import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.LoginResponse;
import com.example.backend.dto.auth.RefreshTokenRequest;
import com.example.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                  HttpServletRequest httpRequest) {
        LoginResponse response = authService.refresh(request.getRefreshToken(), httpRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Si un compte existe avec cet email, un lien de réinitialisation vient de vous être envoyé."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNouveauMotDePasse());
        return ResponseEntity.ok(Map.of("message", "Mot de passe réinitialisé avec succès."));
    }
}
