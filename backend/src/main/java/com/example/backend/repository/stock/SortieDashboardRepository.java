package com.example.backend.repository.stock;

import com.example.backend.entity.DetailSortieMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SortieDashboardRepository extends JpaRepository<DetailSortieMateriel, Integer> {

    @Query("""
        SELECT COALESCE(SUM(d.quantite), 0)
        FROM DetailSortieMateriel d
        WHERE d.sortieMateriel.statut = 'Validée'
        AND d.sortieMateriel.dateSortie BETWEEN :debut AND :fin
        """)
    long sumQuantiteSortiePeriode(@Param("debut") LocalDateTime debut, @Param("fin") LocalDateTime fin);
}