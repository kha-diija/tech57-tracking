package com.example.backend.controller;

import com.example.backend.dto.admin.etablissement.FormateurRequest;
import com.example.backend.dto.admin.etablissement.FormateurResponse;
import com.example.backend.service.admin.FormateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/etablissements/{idEtablissement}/formateurs")
public class FormateurController {

    private final FormateurService formateurService;

    public FormateurController(FormateurService formateurService) {
        this.formateurService = formateurService;
    }

    @GetMapping
    public ResponseEntity<List<FormateurResponse>> getAll(@PathVariable Integer idEtablissement) {
        return ResponseEntity.ok(formateurService.getByEtablissement(idEtablissement));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count(@PathVariable Integer idEtablissement) {
        return ResponseEntity.ok(Map.of("count", formateurService.countByEtablissement(idEtablissement)));
    }

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Integer idEtablissement, @RequestBody FormateurRequest req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(formateurService.create(idEtablissement, req));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{idFormateur}")
    public ResponseEntity<?> update(@PathVariable Integer idEtablissement,
                                    @PathVariable Integer idFormateur,
                                    @RequestBody FormateurRequest req) {
        try {
            return ResponseEntity.ok(formateurService.update(idFormateur, req));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{idFormateur}")
    public ResponseEntity<?> delete(@PathVariable Integer idEtablissement, @PathVariable Integer idFormateur) {
        try {
            formateurService.delete(idFormateur);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}