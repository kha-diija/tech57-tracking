package com.example.backend.repository.technicien;

import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardTechnicienRepository extends JpaRepository<Intervention, Integer> {

    @Query("SELECT i FROM Intervention i JOIN i.technicien t WHERE t.email = :email")
    List<Intervention> findInterventionsByTechnicienEmail(@Param("email") String email);
}