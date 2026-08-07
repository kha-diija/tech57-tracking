package com.example.backend.repository.gestionnairestock;

import com.example.backend.entity.StockMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GsStockMaterielRepository extends JpaRepository<StockMateriel, Integer> {
}