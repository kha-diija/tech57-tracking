package com.example.backend.repository;

import com.example.backend.entity.MissionInstallation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface adminMissionInstallationRepository extends JpaRepository<MissionInstallation, Integer> {

    List<MissionInstallation> findByEtablissement_IdEtablissement(Integer idEtablissement);

    List<MissionInstallation> findByAdministrateur_Id(Integer idAdministrateur);

    List<MissionInstallation> findByStatut(String statut);
}