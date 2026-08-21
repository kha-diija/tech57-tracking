package com.example.backend.repository.admin;

import com.example.backend.entity.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MaterielRepository extends JpaRepository<Materiel, Integer> {
    Optional<Materiel> findByReference(String reference);
}