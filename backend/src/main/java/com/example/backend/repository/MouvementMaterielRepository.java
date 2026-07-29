package com.example.backend.repository;

import com.example.backend.entity.MouvementMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MouvementMaterielRepository extends JpaRepository<MouvementMateriel, Integer> {
    List<MouvementMateriel> findByMateriel_IdMaterielOrderByDateMouvementDesc(Integer idMateriel);
}