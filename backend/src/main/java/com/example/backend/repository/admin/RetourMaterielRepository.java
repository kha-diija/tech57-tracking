package com.example.backend.repository.admin;

import com.example.backend.entity.Intervention;
import com.example.backend.entity.RetourMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetourMaterielRepository extends JpaRepository<RetourMateriel, Integer> {
    List<RetourMateriel> findByIntervention(Intervention intervention);
}