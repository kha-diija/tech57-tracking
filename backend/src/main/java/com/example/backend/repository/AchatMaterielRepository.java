package com.example.backend.repository;

import com.example.backend.entity.AchatMateriel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchatMaterielRepository extends JpaRepository<AchatMateriel, Integer> {
    List<AchatMateriel> findByMateriel_IdMaterielOrderByDateAchatDesc(Integer idMateriel);
    List<AchatMateriel> findAllByOrderByDateAchatDesc();
}