package com.example.backend.repository.admin;

import com.example.backend.entity.CheckInOut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInOutRepository extends JpaRepository<CheckInOut, Integer> {

    // Récupérer toutes les visites d'une intervention ordonnées par numéro de visite
    List<CheckInOut> findByInterventionIdInterventionOrderByNumeroVisiteAsc(Integer idIntervention);

    // Trouver la visite active en cours (Check-in fait mais pas encore de Check-out)
    Optional<CheckInOut> findByInterventionIdInterventionAndDateHeureCheckoutIsNull(Integer idIntervention);
}