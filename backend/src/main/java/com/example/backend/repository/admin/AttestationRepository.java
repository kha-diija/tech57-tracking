package com.example.backend.repository.admin;

import com.example.backend.entity.Attestation;
import com.example.backend.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
// AttestationRepository.java

import java.util.List;

@Repository
public interface AttestationRepository extends JpaRepository<Attestation, Integer> {
    List<Attestation> findByIntervention(Intervention intervention);
    void deleteByIntervention(Intervention intervention);
}