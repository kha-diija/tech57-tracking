package com.example.backend.repository;

import com.example.backend.entity.Observateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ObservateurRepository extends JpaRepository<Observateur, Integer> {
}