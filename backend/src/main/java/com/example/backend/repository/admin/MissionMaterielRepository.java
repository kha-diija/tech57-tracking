package com.example.backend.repository.admin;

import com.example.backend.entity.MissionMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionMaterielRepository extends JpaRepository<MissionMateriel, Integer> {

    // ✅ Trouver les matériels d'une mission
    List<MissionMateriel> findByMission_IdMission(Integer idMission);

    // ✅ Supprimer les matériels d'une mission
    void deleteByMission_IdMission(Integer idMission);
}