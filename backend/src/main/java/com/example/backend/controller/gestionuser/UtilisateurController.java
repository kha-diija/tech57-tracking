package com.example.backend.controller.gestionuser;

import com.example.backend.dto.gestionuser.UserRequestDto;
import com.example.backend.dto.gestionuser.UserResponseDto;
import com.example.backend.dto.gestionuser.UserUpdateRequestDto;
import com.example.backend.service.gestionuser.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des utilisateurs - Réservée aux administrateurs.
 * Nécessite @EnableMethodSecurity sur ta SecurityConfig.
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMINISTRATEUR')")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(utilisateurService.getAllUsers());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto dto) {
        UserResponseDto createdUser = utilisateurService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequestDto dto,
            Authentication authentication) {
        UserResponseDto updatedUser = utilisateurService.updateUser(id, dto, authentication);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserResponseDto> toggleStatus(
            @PathVariable Integer id,
            Authentication authentication) {
        return ResponseEntity.ok(utilisateurService.toggleStatus(id, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Integer id,
            Authentication authentication) {
        utilisateurService.deleteUser(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
