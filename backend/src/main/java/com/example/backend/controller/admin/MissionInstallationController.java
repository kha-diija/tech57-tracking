package com.example.backend.controller.admin; // Adapter selon ton package réel (ex: com.example.backend.controller ou sous-package admin)

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
    public ResponseEntity<Void> deleteMission(@PathVariable Integer id) {
        missionService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }
}