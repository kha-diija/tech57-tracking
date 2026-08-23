package com.example.backend.repository;

import com.example.backend.entity.Observateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObservateurRepositoryy extends JpaRepository<Observateur, Integer> {

    Optional<Observateur> findByEmail(String email);
}