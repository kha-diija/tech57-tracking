package com.example.backend.repository;

import com.example.backend.entity.GestionnaireStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GestionnaireStockRepository extends JpaRepository<GestionnaireStock, Integer> {
}