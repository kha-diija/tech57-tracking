package com.example.backend.repository.stock;

import com.example.backend.entity.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterielDashboardRepository extends JpaRepository<Materiel, Integer> {

    @Query("SELECT m FROM Materiel m WHERE m.etat IN :etats")
    List<Materiel> findByEtatIn(@Param("etats") List<String> etats);

    @Query("SELECT COUNT(m) FROM Materiel m WHERE m.etat IN :etats")
    long countByEtatIn(@Param("etats") List<String> etats);
}