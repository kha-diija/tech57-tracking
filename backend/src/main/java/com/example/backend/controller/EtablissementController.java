package com.example.backend.controller;

import com.example.backend.dto.admin.etablissement.EtablissementKpiResponse;
import com.example.backend.dto.admin.etablissement.EtablissementRequest;
import com.example.backend.dto.admin.etablissement.EtablissementResponse;
import com.example.backend.service.admin.EtablissementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.admin.etablissement.EtablissementImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/etablissements")
public class EtablissementController {

    private final EtablissementService etablissementService;

    public EtablissementController(EtablissementService etablissementService) {
        this.etablissementService = etablissementService;
    }

    @GetMapping
    public ResponseEntity<List<EtablissementResponse>> getAll() {
        return ResponseEntity.ok(etablissementService.getAll());
    }

    @GetMapping("/kpis")
    public ResponseEntity<EtablissementKpiResponse> getKpis() {
        return ResponseEntity.ok(etablissementService.getKpis());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EtablissementResponse> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(etablissementService.getById(id));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<EtablissementResponse> create(@RequestBody EtablissementRequest request) {
        EtablissementResponse created = etablissementService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EtablissementResponse> update(@PathVariable Integer id,
                                                        @RequestBody EtablissementRequest request) {
        try {
            return ResponseEntity.ok(etablissementService.update(id, request));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATEUR')")
    public ResponseEntity<?> delete(@PathVariable Integer id,
                                    @RequestParam(required = false, defaultValue = "false") boolean force) {
        try {
            etablissementService.delete(id, force);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping(value = "/import", consumes = "multipart/form-data")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file,
                                         @RequestParam("idProvince") Integer idProvince) {
        try {
            EtablissementImportResult result = etablissementService.importFromExcel(file, idProvince);
            return ResponseEntity.ok(result);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}