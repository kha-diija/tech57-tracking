
package com.example.backend.repository.admin;

import com.example.backend.entity.ChecklistEquipement;
import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChecklistEquipementRepository extends JpaRepository<ChecklistEquipement, Integer> {
    List<ChecklistEquipement> findByIntervention(Intervention intervention);
    void deleteByIntervention(Intervention intervention);
}