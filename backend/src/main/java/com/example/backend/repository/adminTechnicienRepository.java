package com.example.backend.repository;

import com.example.backend.entity.Technicien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface adminTechnicienRepository extends JpaRepository<Technicien, Integer> {
}