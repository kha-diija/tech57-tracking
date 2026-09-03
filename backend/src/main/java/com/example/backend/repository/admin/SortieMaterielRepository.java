package com.example.backend.repository.admin;

import com.example.backend.entity.Intervention;
import com.example.backend.entity.SortieMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SortieMaterielRepository extends JpaRepository<SortieMateriel, Integer> {
    List<SortieMateriel> findByIntervention(Intervention intervention);
    List<SortieMateriel> findByStatut(String statut);

    // ✅ REQUÊTE EXPLICITE : Jointure avec le technicien pour récupérer par email
    @Query("SELECT s FROM SortieMateriel s JOIN s.technicien t WHERE t.email = :email")
    List<SortieMateriel> findByTechnicienEmail(@Param("email") String email);

    List<SortieMateriel> findByMissionIdMission(Integer idMission);

    @Query("SELECT s FROM SortieMateriel s WHERE s.intervention.idIntervention IN :ids")
    List<SortieMateriel> findByInterventionIds(@Param("ids") List<Integer> ids);
}