package com.example.backend.repository.admin;

import com.example.backend.entity.Rapport;
import com.example.backend.entity.Intervention;
import com.example.backend.entity.MissionInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RapportRepository extends JpaRepository<Rapport, Integer> {
    void deleteByIntervention(Intervention intervention);
    void deleteByMission(MissionInstallation mission);
}