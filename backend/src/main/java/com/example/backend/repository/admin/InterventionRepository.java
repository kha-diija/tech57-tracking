package com.example.backend.repository.admin;

import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, Integer> {

    List<Intervention> findByMissionIdMission(Integer missionId);

    List<Intervention> findByTechnicienId(Integer technicienId);

    List<Intervention> findByStatut(String statut);

    // Recherche des interventions planifiées dont la date prévue est dépassée (pour le statut "En retard")
    List<Intervention> findByStatutAndDatePrevueBefore(String statut, LocalDateTime date);
}