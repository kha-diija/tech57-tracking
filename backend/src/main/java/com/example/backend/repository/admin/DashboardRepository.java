package com.example.backend.repository.admin;

import com.example.backend.entity.MissionInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<MissionInstallation, Long> {

    // Compte les missions actives (en se basant sur ton entité MissionInstallation)
    @Query("SELECT COUNT(m) FROM MissionInstallation m WHERE m.statut = 'ACTIVE'")
    long countActiveMissions();

    // Compte le nombre total d'établissements suivis
    @Query("SELECT COUNT(e) FROM Etablissement e")
    long countEtablissementsSuivis();

    // Compte les techniciens dont le compte est actif (basé sur la classe mère Utilisateur)
    @Query("SELECT COUNT(t) FROM Technicien t WHERE t.compteActif = true")
    long countTechniciensDisponibles();

}