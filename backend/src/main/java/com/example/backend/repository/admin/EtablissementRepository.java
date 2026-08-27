package com.example.backend.repository.admin;

import com.example.backend.entity.Etablissement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtablissementRepository extends JpaRepository<Etablissement, Integer> {

    @Query("SELECT e FROM Etablissement e " +
            "LEFT JOIN FETCH e.commune c " +
            "LEFT JOIN FETCH c.province p " +
            "LEFT JOIN FETCH p.region r " +
            "LEFT JOIN FETCH e.responsable " +
            "ORDER BY e.designation ASC")
    List<Etablissement> findAllWithDetails();

    @Query("SELECT e FROM Etablissement e " +
            "LEFT JOIN FETCH e.commune c " +
            "LEFT JOIN FETCH c.province p " +
            "LEFT JOIN FETCH p.region r " +
            "LEFT JOIN FETCH e.responsable " +
            "WHERE e.idEtablissement = :id")
    Optional<Etablissement> findByIdWithDetails(@Param("id") Integer id);

    boolean existsByReference(String reference);

    @Query("SELECT COUNT(e) FROM Etablissement e WHERE e.responsable IS NULL")
    long countSansResponsable();

    @Query("SELECT COUNT(DISTINCT c.province.region.idRegion) FROM Etablissement e JOIN e.commune c")
    long countRegionsCouvertes();

    // Ajout pour compter les matériels liés
    @Query("SELECT COUNT(m) FROM Materiel m WHERE m.etablissement.idEtablissement = :id")
    long countMaterielsByEtablissementId(@Param("id") Integer id);

    Optional<Etablissement> findByReference(String reference);
    @Query("SELECT e FROM Etablissement e " +
            "LEFT JOIN FETCH e.responsable " +
            "WHERE e.commune.idCommune = :idCommune " +
            "ORDER BY e.designation ASC")
    List<Etablissement> findByCommune_IdCommune(@Param("idCommune") Integer idCommune);
}
