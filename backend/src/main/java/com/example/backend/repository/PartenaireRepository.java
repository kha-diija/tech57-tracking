package com.example.backend.repository;

import com.example.backend.entity.Partenaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PartenaireRepository extends JpaRepository<Partenaire, Integer> {
    Optional<Partenaire> findByEmail(String email);
}