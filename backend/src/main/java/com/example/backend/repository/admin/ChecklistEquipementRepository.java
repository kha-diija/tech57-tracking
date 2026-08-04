package com.example.backend.repository.admin;

import com.example.backend.entity.ChecklistEquipement;
import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChecklistEquipementRepository extends JpaRepository<ChecklistEquipement, Integer> {
    Optional<ChecklistEquipement> findByIntervention(Intervention intervention);
}