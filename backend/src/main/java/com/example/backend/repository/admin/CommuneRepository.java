package com.example.backend.repository.admin;

import com.example.backend.entity.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Integer> {
    List<Commune> findByProvince_IdProvince(Integer idProvince);
    Optional<Commune> findByNomIgnoreCaseAndProvince_IdProvince(String nom, Integer idProvince);
}