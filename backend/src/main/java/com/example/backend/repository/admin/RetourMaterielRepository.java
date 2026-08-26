package com.example.backend.repository.admin;

import com.example.backend.entity.Intervention;
import com.example.backend.entity.RetourMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetourMaterielRepository extends JpaRepository<RetourMateriel, Integer> {
    List<RetourMateriel> findByIntervention(Intervention intervention);
    @Query("SELECT r FROM RetourMateriel r WHERE r.intervention.idIntervention IN :ids")
    List<RetourMateriel> findByInterventionIds(@Param("ids") List<Integer> ids);
}