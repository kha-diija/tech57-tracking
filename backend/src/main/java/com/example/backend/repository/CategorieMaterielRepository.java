package com.example.backend.repository;

import com.example.backend.entity.CategorieMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieMaterielRepository extends JpaRepository<CategorieMateriel, Integer> {
    Optional<CategorieMateriel> findByNom(String nom);
}