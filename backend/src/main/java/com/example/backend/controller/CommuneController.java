package com.example.backend.controller;

import com.example.backend.dto.admin.etablissement.CommuneDTO;
import com.example.backend.entity.Commune;
import com.example.backend.repository.admin.CommuneRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/communes")
@CrossOrigin(origins = "*")
public class CommuneController {

    private final CommuneRepository communeRepository;

    public CommuneController(CommuneRepository communeRepository) {
        this.communeRepository = communeRepository;
    }

    @GetMapping
    public ResponseEntity<List<CommuneDTO>> getAllCommunes() {
        List<Commune> communes = communeRepository.findAll();
        List<CommuneDTO> dtos = communes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommuneDTO> getCommuneById(@PathVariable Integer id) {
        return communeRepository.findById(id)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/province/{idProvince}")
    public ResponseEntity<List<CommuneDTO>> getCommunesByProvince(@PathVariable Integer idProvince) {
        List<Commune> communes = communeRepository.findByProvince_IdProvince(idProvince);
        List<CommuneDTO> dtos = communes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private CommuneDTO toDTO(Commune commune) {
        CommuneDTO dto = new CommuneDTO();
        dto.setIdCommune(commune.getIdCommune());
        dto.setNom(commune.getNom());
        dto.setCode(commune.getCode());
        if (commune.getProvince() != null) {
            dto.setIdProvince(commune.getProvince().getIdProvince());
            dto.setProvinceNom(commune.getProvince().getNom());
        }
        return dto;
    }
}