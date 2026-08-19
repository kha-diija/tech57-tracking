package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.repository.MouvementMaterielRepository;
import com.example.backend.service.MaterielService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiels")
@RequiredArgsConstructor
public class MaterielController {

    private final MaterielService materielService;
    private final MouvementMaterielRepository mouvementMaterielRepository;

    // --- LECTURE : accessible à tous les rôles authentifiés (ADMIN, GESTIONNAIRE_STOCK, TECHNICIEN, OBSERVATEUR) ---

    @GetMapping
    public Page<MaterielDTO> rechercher(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String etat,
            @RequestParam(required = false) Integer idCategorie,
            @RequestParam(required = false) Integer idEtablissement,
            @RequestParam(defaultValue = "true") boolean topLevelOnly,
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return materielService.rechercher(search, etat, idCategorie, idEtablissement, topLevelOnly, pageable);
    }

    @GetMapping("/{id}")
    public MaterielDTO getById(@PathVariable Integer id) {
        return materielService.getById(id);
    }

    @GetMapping("/{id}/mouvements")
    public List<MouvementMaterielDTO> getMouvements(@PathVariable Integer id) {
        return mouvementMaterielRepository.findByMateriel_IdMaterielOrderByDateMouvementDesc(id)
                .stream()
                .map(mv -> MouvementMaterielDTO.builder()
                        .idMouvement(mv.getIdMouvement())
                        .type(mv.getType())
                        .dateMouvement(mv.getDateMouvement())
                        .origine(mv.getOrigine())
                        .destination(mv.getDestination())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    // --- ÉCRITURE : réservée à ADMINISTRATEUR et GESTIONNAIRE_STOCK ---

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PostMapping
    public ResponseEntity<MaterielDTO> creerSimple(@Valid @RequestBody MaterielRequest request) {
        MaterielDTO created = materielService.creerSimple(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PostMapping("/kits")
    public ResponseEntity<MaterielDTO> creerKit(@Valid @RequestBody KitRequest request) {
        MaterielDTO created = materielService.creerKit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PutMapping("/{id}")
    public MaterielDTO modifier(@PathVariable Integer id, @Valid @RequestBody MaterielRequest request) {
        return materielService.modifier(id, request);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        materielService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/materiels/{id}/etat  { "etat": "En panne" }
    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PatchMapping("/{id}/etat")
    public MaterielDTO changerEtat(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return materielService.changerEtat(id, body.get("etat"));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PatchMapping("/{id}/marquer-maintenance")
    public MaterielDTO marquerEnMaintenance(@PathVariable Integer id) {
        return materielService.marquerEnMaintenance(id);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PostMapping("/{idKit}/composants")
    public MaterielDTO ajouterComposant(@PathVariable Integer idKit, @Valid @RequestBody ComposantRequest request) {
        return materielService.ajouterComposant(idKit, request);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @DeleteMapping("/composants/{idComposant}")
    public ResponseEntity<Void> retirerComposant(@PathVariable Integer idComposant) {
        materielService.retirerComposant(idComposant);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMINISTRATEUR','GESTIONNAIRE_STOCK')")
    @PostMapping("/{id}/code-qr")
    public Map<String, String> regenererCodeQr(@PathVariable Integer id) {
        return Map.of("codeQr", materielService.regenererCodeQr(id));
    }
}