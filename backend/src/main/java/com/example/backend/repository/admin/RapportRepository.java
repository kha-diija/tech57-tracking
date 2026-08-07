package com.example.backend.repository.admin;

import com.example.backend.entity.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Integer> {
}