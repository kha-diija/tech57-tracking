package com.example.backend.controller.gestionnairestock;

import com.example.backend.dto.gestionnairestock.*;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.gestionnairestock.SortieMaterielGestionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestionnaire-stock/sorties")
@PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
public class SortieMaterielGestionController {

    private final SortieMaterielGestionService service;

    public SortieMaterielGestionController(SortieMaterielGestionService service) {
        this.service = service;
    }

    @GetMapping
    public List<SortieMaterielDto> lister(@RequestParam(defaultValue = "En attente") String statut) {
        return service.listerParStatut(statut);
    }

    @PostMapping("/{id}/approuver")
    public SortieMaterielDto approuver(@PathVariable Integer id,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        return service.approuver(id, principal.getId());
    }

    @PostMapping("/{id}/rejeter")
    public SortieMaterielDto rejeter(@PathVariable Integer id,
                                     @Valid @RequestBody RejeterSortieRequest request,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return service.rejeter(id, principal.getId(), request);
    }
}