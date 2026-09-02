package com.example.backend.controller.admin;

import com.example.backend.dto.admin.Mission.MissionRequestDTO;
import com.example.backend.dto.admin.Mission.MissionResponseDTO;
import com.example.backend.dto.admin.etablissement.CommuneResponse;
import com.example.backend.dto.admin.etablissement.ProvinceResponse;
import com.example.backend.entity.Commune;
import com.example.backend.entity.Province;
import com.example.backend.repository.admin.CommuneRepository;
import com.example.backend.repository.admin.ProvinceRepository;
import com.example.backend.security.UserPrincipal;
import com.example.backend.service.admin.MissionInstallationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/missions")
@CrossOrigin(origins = "*")
public class MissionInstallationController {

    @Autowired
    private MissionInstallationService missionService;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private CommuneRepository communeRepository;

    @GetMapping
    public ResponseEntity<List<MissionResponseDTO>> getAllMissions() {
        List<MissionResponseDTO> missions = missionService.getAllMissions();
        return ResponseEntity.ok(missions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MissionResponseDTO> getMissionById(@PathVariable Integer id) {
        MissionResponseDTO mission = missionService.getMissionById(id);
        return ResponseEntity.ok(mission);
    }

    @PostMapping
    public ResponseEntity<MissionResponseDTO> createMission(@Valid @RequestBody MissionRequestDTO dto) {
        MissionResponseDTO created = missionService.createMission(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MissionResponseDTO> updateMission(
            @PathVariable Integer id,
            @Valid @RequestBody MissionRequestDTO dto) {
        MissionResponseDTO updated = missionService.updateMission(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMission(@PathVariable Integer id,
                                           @RequestParam(required = false, defaultValue = "false") boolean force) {
        try {
            missionService.deleteMission(id, force);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    // ✅ ENDPOINT : Approuver la mission
    @PutMapping("/{id}/approuver")
    public ResponseEntity<MissionResponseDTO> approuverMission(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        MissionResponseDTO updated = missionService.approuverMission(id, principal.getId());
        return ResponseEntity.ok(updated);
    }

    // ✅ ENDPOINT : Rejeter la mission
    @DeleteMapping("/{id}/rejeter")
    public ResponseEntity<?> rejeterMission(
            @PathVariable Integer id,
            @RequestParam String motif,
            @AuthenticationPrincipal UserPrincipal principal) {
        try {
            missionService.rejeterMission(id, motif, principal.getId());
            return ResponseEntity.ok(Map.of("message", "Mission rejetée avec succès"));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    // ✅ NOUVEAU ENDPOINT : Récupérer les provinces par région
    @GetMapping("/provinces/region/{idRegion}")
    public ResponseEntity<List<ProvinceResponse>> getProvincesByRegion(@PathVariable Integer idRegion) {
        List<Province> provinces = provinceRepository.findByRegion_IdRegion(idRegion);
        List<ProvinceResponse> responses = provinces.stream()
                .map(p -> new ProvinceResponse(p.getIdProvince(), p.getNom(), p.getCode(), p.getRegion().getIdRegion()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ✅ NOUVEAU ENDPOINT : Récupérer les communes par province
    // ✅ ENDPOINT : Récupérer les communes par province (avec nom de la province)
    @GetMapping("/communes/province/{idProvince}")
    public ResponseEntity<List<CommuneResponse>> getCommunesByProvince(@PathVariable Integer idProvince) {
        List<CommuneResponse> responses = communeRepository.findCommunesWithProvinceName(idProvince);
        return ResponseEntity.ok(responses);
    }
}