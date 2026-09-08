package com.example.backend.repository;

import com.example.backend.entity.SimulateurTrajet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimulateurTrajetRepository extends JpaRepository<SimulateurTrajet, Integer> {

    List<SimulateurTrajet> findByTechnicien_IdOrderByIdSimulationDesc(Integer idTechnicien);

    List<SimulateurTrajet> findByMission_IdMission(Integer idMission);
}