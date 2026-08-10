package com.example.backend.controller.admin;

import com.example.backend.dto.admin.Mission.MissionRequestDTO;
import com.example.backend.dto.admin.Mission.MissionResponseDTO;
import com.example.backend.service.admin.MissionInstallationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/missions")
@CrossOrigin(origins = "*") // Permet les appels depuis le front-end Angular (localhost:4200)
public class MissionInstallationController {

    @Autowired
    private MissionInstallationService missionService;

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
            // 409 Conflict : la mission a des dépendances, message renvoyé au front pour confirmation
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            // Mission introuvable
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }
}