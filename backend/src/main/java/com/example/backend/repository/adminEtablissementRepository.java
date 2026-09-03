package com.example.backend.repository;

import com.example.backend.entity.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface adminEtablissementRepository extends JpaRepository<Etablissement, Integer>,
        JpaSpecificationExecutor<Etablissement> {

    Optional<Etablissement> findByReference(String reference);

    boolean existsByReference(String reference);

    List<Etablissement> findByCommune_IdCommune(Integer idCommune);

    List<Etablissement> findByResponsable_IdResponsable(Integer idResponsable);

    List<Etablissement> findByType(String type);
}