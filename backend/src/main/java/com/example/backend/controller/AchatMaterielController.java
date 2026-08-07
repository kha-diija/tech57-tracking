package com.example.backend.controller;

import com.example.backend.dto.AchatMaterielDto;
import com.example.backend.dto.CreerAchatRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.AchatMaterielService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achats-materiel")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
public class AchatMaterielController {

    private final AchatMaterielService service;

    public AchatMaterielController(AchatMaterielService service) {
        this.service = service;
    }

    @GetMapping
    public List<AchatMaterielDto> lister() {
        return service.lister();
    }

    @GetMapping("/materiel/{idMateriel}")
    public List<AchatMaterielDto> listerParMateriel(@PathVariable Integer idMateriel) {
        return service.listerParMateriel(idMateriel);
    }

    @PostMapping
    public ResponseEntity<AchatMaterielDto> creer(@Valid @RequestBody CreerAchatRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        AchatMaterielDto created = service.creerAchat(request, principal.getId(), principal.getTypeUtilisateur());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}