package com.example.backend.repository.admin;

import com.example.backend.entity.EquipeTechnique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipeTechniqueRepository extends JpaRepository<EquipeTechnique, Integer> {
}