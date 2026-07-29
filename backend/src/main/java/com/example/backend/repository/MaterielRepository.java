package com.example.backend.repository;

import com.example.backend.entity.Materiel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MaterielRepository extends JpaRepository<Materiel, Integer>,
        JpaSpecificationExecutor<Materiel> {

    Optional<Materiel> findByReference(String reference);

    Optional<Materiel> findByCodeQr(String codeQr);

    boolean existsByReference(String reference);

    boolean existsByCodeQr(String codeQr);

    // Composants directs d'un kit
    List<Materiel> findByMaterielParent_IdMateriel(Integer idMaterielParent);
}