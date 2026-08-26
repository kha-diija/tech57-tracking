package com.example.backend.repository.admin;

import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // Charge la liste complète en 1 seule requête (évite le problème N+1 sur mission/technicien/checkInOuts)
    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT i FROM Intervention i " +
                    "LEFT JOIN FETCH i.mission " +
                    "LEFT JOIN FETCH i.technicien " +
                    "LEFT JOIN FETCH i.checkInOuts"
    )
    List<Intervention> findAllWithMissionAndTechnicien();
    @Query("SELECT DISTINCT i FROM Intervention i " +
            "LEFT JOIN FETCH i.mission m " +                    // ← Alias "m" pour la mission
            "LEFT JOIN FETCH m.etablissement " +                // ← AJOUT : Charger l'établissement
            "LEFT JOIN FETCH i.technicien " +
            "LEFT JOIN FETCH i.checkInOuts " +
            "WHERE i.technicien.id = :technicienId")
    List<Intervention> findByTechnicienIdWithMissionAndCheckIns(Integer technicienId);

}