package com.example.backend.repository.admin;

import com.example.backend.entity.MissionInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MissionInstallationRepository extends JpaRepository<MissionInstallation, Integer> {

    // Compter le nombre de missions liées à un établissement
    @Query("SELECT COUNT(m) FROM MissionInstallation m WHERE m.etablissement.idEtablissement = :idEtablissement")
    long countByEtablissementIdEtablissement(@Param("idEtablissement") Integer idEtablissement);

    // Supprimer les missions liées à un établissement en mode force
    @Modifying
    @Query("DELETE FROM MissionInstallation m WHERE m.etablissement.idEtablissement = :idEtablissement")
    void deleteByEtablissementIdEtablissement(@Param("idEtablissement") Integer idEtablissement);
}