package com.example.backend.controller;

import com.example.backend.dto.SimulateurTrajetDTO;
import com.example.backend.dto.SimulateurTrajetRequest;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.SimulateurTrajetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/simulateur-trajet")
public class SimulateurTrajetController {

    private static final Logger log = LoggerFactory.getLogger(SimulateurTrajetController.class);

    private final SimulateurTrajetService simulateurTrajetService;

    public SimulateurTrajetController(SimulateurTrajetService simulateurTrajetService) {
        this.simulateurTrajetService = simulateurTrajetService;
    }

    // -------------------------------------------------------------------------
    // 1. Calcul d’un itinéraire (persisté)
    // -------------------------------------------------------------------------
    @PreAuthorize("hasAnyRole('TECHNICIEN', 'ADMINISTRATEUR')")
    @PostMapping("/calculer")
    public ResponseEntity<?> calculer(@Valid @RequestBody SimulateurTrajetRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Calcul d'itinéraire reçu : origine={}, destination={}, type={}, prix={}",
                request.getOrigine(), request.getDestination(), request.getTypeRoute(), request.getPrixCarburantLitre());

        // Vérification de l'authentification (normalement déjà assurée par Spring Security)
        if (principal == null) {
            log.error("Aucun principal trouvé dans le contexte de sécurité");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Utilisateur non authentifié");
        }

        try {
            boolean estAdmin = "ADMINISTRATEUR".equals(principal.getTypeUtilisateur());
            SimulateurTrajetDTO result = simulateurTrajetService.calculer(request, principal.getId(), estAdmin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors du calcul d'itinéraire", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 2. Comparer Autoroute vs Nationale (ne persiste pas)
    // -------------------------------------------------------------------------
    @PreAuthorize("hasAnyRole('TECHNICIEN', 'ADMINISTRATEUR')")
    @PostMapping("/comparer")
    public ResponseEntity<?> comparer(@Valid @RequestBody SimulateurTrajetRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        log.info("Comparaison d'itinéraires reçue : {} -> {}", request.getOrigine(), request.getDestination());

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Utilisateur non authentifié");
        }

        try {
            boolean estAdmin = "ADMINISTRATEUR".equals(principal.getTypeUtilisateur());
            List<SimulateurTrajetDTO> resultats = simulateurTrajetService.comparerItineraires(
                    request, principal.getId(), estAdmin);
            return ResponseEntity.ok(resultats);
        } catch (Exception e) {
            log.error("Erreur lors de la comparaison d'itinéraires", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 3. Proposer un budget à une mission (rattache une simulation existante)
    // -------------------------------------------------------------------------
    @PreAuthorize("hasAnyRole('TECHNICIEN', 'ADMINISTRATEUR')")
    @PostMapping("/{idSimulation}/proposer-budget/{idMission}")
    public ResponseEntity<?> proposerBudget(@PathVariable Integer idSimulation,
                                            @PathVariable Integer idMission) {
        log.info("Proposition de budget : simulation {} pour mission {}", idSimulation, idMission);

        try {
            SimulateurTrajetDTO result = simulateurTrajetService.proposerBudgetMission(idSimulation, idMission);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Erreur lors de la proposition de budget", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // 4. Historique des simulations d’un technicien
    // -------------------------------------------------------------------------
    @PreAuthorize("hasAnyRole('TECHNICIEN', 'ADMINISTRATEUR')")
    @GetMapping("/historique/{idTechnicien}")
    public ResponseEntity<?> historique(@PathVariable Integer idTechnicien) {
        log.info("Consultation de l'historique pour le technicien {}", idTechnicien);

        try {
            List<SimulateurTrajetDTO> historique = simulateurTrajetService.historiqueParTechnicien(idTechnicien);
            return ResponseEntity.ok(historique);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'historique", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }
}