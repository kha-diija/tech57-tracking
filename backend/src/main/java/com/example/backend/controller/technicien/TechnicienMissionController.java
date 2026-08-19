package com.example.backend.controller.technicien;

import com.example.backend.dto.admin.Mission.MissionRequestDTO;
import com.example.backend.dto.admin.Mission.MissionResponseDTO;
import com.example.backend.service.admin.MissionInstallationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicien/missions")
@CrossOrigin(origins = "*")
public class TechnicienMissionController {

    @Autowired
    private MissionInstallationService missionService;

    // 1. Voir uniquement les missions de l'équipe du technicien connecté
    @GetMapping
    public ResponseEntity<List<MissionResponseDTO>> getMissionsForCurrentTechnicien(@RequestParam Integer idTechnicien) {
        List<MissionResponseDTO> missions = missionService.getMissionsByTechnicienEquipe(idTechnicien);
        return ResponseEntity.ok(missions);
    }

    // 2. Créer une mission (affectation automatique à son équipe + notification temps réel des admins)
    @PostMapping
    public ResponseEntity<MissionResponseDTO> createMission(
            @Valid @RequestBody MissionRequestDTO dto,
            @RequestParam Integer idTechnicien) {
        MissionResponseDTO created = missionService.createMissionByTechnicien(dto, idTechnicien);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 3. Modifier une mission (pas de suppression permise ici, conformément au cahier des charges)
    @PutMapping("/{id}")
    public ResponseEntity<MissionResponseDTO> updateMission(
            @PathVariable Integer id,
            @Valid @RequestBody MissionRequestDTO dto) {
        MissionResponseDTO updated = missionService.updateMission(id, dto);
        return ResponseEntity.ok(updated);
    }
}