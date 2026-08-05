package com.example.backend.repository;

import com.example.backend.entity.MissionInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface adminMissionInstallationRepository extends JpaRepository<MissionInstallation, Integer> {

    List<MissionInstallation> findByEtablissement_IdEtablissement(Integer idEtablissement);

    List<MissionInstallation> findByAdministrateur_Id(Integer idAdministrateur);

    List<MissionInstallation> findByStatut(String statut);

    // Compter le nombre de missions liées à un établissement
    @Query("SELECT COUNT(m) FROM MissionInstallation m WHERE m.etablissement.idEtablissement = :idEtablissement")
    long countByEtablissementIdEtablissement(@Param("idEtablissement") Integer idEtablissement);

    // Supprimer les missions liées à un établissement en mode force
    @Modifying
    @Query("DELETE FROM MissionInstallation m WHERE m.etablissement.idEtablissement = :idEtablissement")
    void deleteByEtablissementIdEtablissement(@Param("idEtablissement") Integer idEtablissement);
}