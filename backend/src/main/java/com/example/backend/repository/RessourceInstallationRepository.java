package com.example.backend.repository;

import com.example.backend.entity.RessourceInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RessourceInstallationRepository extends JpaRepository<RessourceInstallation, Integer> {
}