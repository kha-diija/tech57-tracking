package com.example.backend.repository.admin;

import com.example.backend.entity.Attestation;
import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttestationRepository extends JpaRepository<Attestation, Integer> {
    Optional<Attestation> findByIntervention(Intervention intervention);
}