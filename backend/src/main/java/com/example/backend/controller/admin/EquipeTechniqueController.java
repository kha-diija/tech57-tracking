package com.example.backend.controller.admin;

import com.example.backend.dto.admin.Mission.EquipeTechniqueResponseDTO;
import com.example.backend.repository.admin.EquipeTechniqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/equipes")
@CrossOrigin(origins = "http://localhost:4200")
public class EquipeTechniqueController {

    @Autowired
    private EquipeTechniqueRepository equipeTechniqueRepository;

    @GetMapping
    public List<EquipeTechniqueResponseDTO> getAllEquipes() {
        return equipeTechniqueRepository.findAll().stream()
                .map(eq -> new EquipeTechniqueResponseDTO(eq.getIdEquipe(), eq.getNomEquipe()))
                .collect(Collectors.toList());
    }
}