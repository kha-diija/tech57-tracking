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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/materiels")
@RequiredArgsConstructor
public class MaterielController {

    private final MaterielService materielService;
    private final MouvementMaterielRepository mouvementMaterielRepository;

    // GET /api/materiels?search=&etat=&idCategorie=&idEtablissement=&topLevelOnly=true&page=0&size=20&sort=nom,asc
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

    @PostMapping
    public ResponseEntity<MaterielDTO> creerSimple(@Valid @RequestBody MaterielRequest request) {
        MaterielDTO created = materielService.creerSimple(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/kits")
    public ResponseEntity<MaterielDTO> creerKit(@Valid @RequestBody KitRequest request) {
        MaterielDTO created = materielService.creerKit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public MaterielDTO modifier(@PathVariable Integer id, @Valid @RequestBody MaterielRequest request) {
        return materielService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        materielService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/materiels/{id}/etat  { "etat": "En panne" }
    @PatchMapping("/{id}/etat")
    public MaterielDTO changerEtat(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return materielService.changerEtat(id, body.get("etat"));
    }

    @PatchMapping("/{id}/marquer-maintenance")
    public MaterielDTO marquerEnMaintenance(@PathVariable Integer id) {
        return materielService.marquerEnMaintenance(id);
    }

    @PostMapping("/{idKit}/composants")
    public MaterielDTO ajouterComposant(@PathVariable Integer idKit, @Valid @RequestBody ComposantRequest request) {
        return materielService.ajouterComposant(idKit, request);
    }

    @DeleteMapping("/composants/{idComposant}")
    public ResponseEntity<Void> retirerComposant(@PathVariable Integer idComposant) {
        materielService.retirerComposant(idComposant);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/code-qr")
    public Map<String, String> regenererCodeQr(@PathVariable Integer id) {
        return Map.of("codeQr", materielService.regenererCodeQr(id));
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
}