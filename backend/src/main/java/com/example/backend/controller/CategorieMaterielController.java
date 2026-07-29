package com.example.backend.controller;

import com.example.backend.dto.CategorieMaterielDTO;
import com.example.backend.repository.CategorieMaterielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories-materiel")
@RequiredArgsConstructor
public class CategorieMaterielController {

    private final CategorieMaterielRepository categorieMaterielRepository;

    @GetMapping
    public List<CategorieMaterielDTO> getAll() {
        return categorieMaterielRepository.findAll().stream()
                .map(c -> CategorieMaterielDTO.builder()
                        .idCategorie(c.getIdCategorie())
                        .nom(c.getNom())
                        .estKit(c.getEstKit())
                        .build())
                .collect(Collectors.toList());
    }
}