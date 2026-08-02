package com.example.backend.controller;

import com.example.backend.dto.SimulateurTrajetDTO;
import com.example.backend.dto.SimulateurTrajetRequest;
import com.example.backend.security.UserPrincipal; // adapte au principal réellement utilisé par ton JwtAuthFilter
import com.example.backend.service.SimulateurTrajetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulateur-trajet")
public class SimulateurTrajetController {

    private final SimulateurTrajetService simulateurTrajetService;

    public SimulateurTrajetController(SimulateurTrajetService simulateurTrajetService) {
        this.simulateurTrajetService = simulateurTrajetService;
    }

    // Technicien : outil principal, usage quotidien
    @PreAuthorize("hasAnyRole('TECHNICIEN','ADMINISTRATEUR')")
    @PostMapping("/calculer")
    public ResponseEntity<SimulateurTrajetDTO> calculer(@Valid @RequestBody SimulateurTrajetRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        boolean estAdmin = "ADMINISTRATEUR".equals(principal.getTypeUtilisateur());
        return ResponseEntity.ok(simulateurTrajetService.calculer(request, principal.getId(), estAdmin));
    }

    // Bouton "Comparer les 2 itinéraires" (Autoroute vs Nationale)
    @PreAuthorize("hasAnyRole('TECHNICIEN','ADMINISTRATEUR')")
    @PostMapping("/comparer")
    public ResponseEntity<List<SimulateurTrajetDTO>> comparer(@Valid @RequestBody SimulateurTrajetRequest request,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        boolean estAdmin = "ADMINISTRATEUR".equals(principal.getTypeUtilisateur());
        return ResponseEntity.ok(simulateurTrajetService.comparerItineraires(request, principal.getId(), estAdmin));
    }

    // Bouton "Utiliser cette offre pour ma mission" / "Proposer ce budget à la mission"
    @PreAuthorize("hasAnyRole('TECHNICIEN','ADMINISTRATEUR')")
    @PostMapping("/{idSimulation}/proposer-budget/{idMission}")
    public ResponseEntity<SimulateurTrajetDTO> proposerBudget(@PathVariable Integer idSimulation,
                                                              @PathVariable Integer idMission) {
        return ResponseEntity.ok(simulateurTrajetService.proposerBudgetMission(idSimulation, idMission));
    }

    @PreAuthorize("hasAnyRole('TECHNICIEN','ADMINISTRATEUR')")
    @GetMapping("/historique/{idTechnicien}")
    public ResponseEntity<List<SimulateurTrajetDTO>> historique(@PathVariable Integer idTechnicien) {
        return ResponseEntity.ok(simulateurTrajetService.historiqueParTechnicien(idTechnicien));
    }
}