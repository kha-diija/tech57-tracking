package com.example.backend.repository.admin;

import com.example.backend.entity.Intervention;
import com.example.backend.entity.SortieMateriel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SortieMaterielRepository extends JpaRepository<SortieMateriel, Integer> {
    List<SortieMateriel> findByIntervention(Intervention intervention);
}