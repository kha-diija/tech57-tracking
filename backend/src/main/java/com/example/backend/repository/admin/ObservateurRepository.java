package com.example.backend.repository.admin;

import com.example.backend.entity.Observateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservateurRepository extends JpaRepository<Observateur, Integer> {
    List<Observateur> findByEtablissement_IdEtablissement(Integer idEtablissement);
    long countByEtablissement_IdEtablissement(Integer idEtablissement);
}