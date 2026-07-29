package com.example.backend.controller;

import com.example.backend.dto.MaintenanceDTO;
import com.example.backend.dto.MaintenanceRequest;
import com.example.backend.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenances")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping("/materiel/{idMateriel}")
    public List<MaintenanceDTO> getHistorique(@PathVariable Integer idMateriel) {
        return maintenanceService.getHistoriqueByMateriel(idMateriel);
    }

    @GetMapping("/{id}")
    public MaintenanceDTO getById(@PathVariable Integer id) {
        return maintenanceService.getById(id);
    }

    @PostMapping
    public ResponseEntity<MaintenanceDTO> creer(@Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maintenanceService.creer(request));
    }

    @PutMapping("/{id}")
    public MaintenanceDTO modifier(@PathVariable Integer id, @Valid @RequestBody MaintenanceRequest request) {
        return maintenanceService.modifier(id, request);
    }

    @PatchMapping("/{id}/cloturer")
    public MaintenanceDTO cloturer(@PathVariable Integer id) {
        return maintenanceService.cloturer(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Integer id) {
        maintenanceService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}