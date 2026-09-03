package com.example.backend.service;

import com.example.backend.dto.SimulateurTrajetDTO;
import com.example.backend.dto.SimulateurTrajetRequest;

import java.util.List;

public interface SimulateurTrajetService {

    /**
     * Calcule le meilleur itinéraire (distance réelle, temps, coûts) entre deux
     * établissements via le moteur de routage, puis persiste le résultat.
     * @param idUtilisateurConnecte id du technicien ou administrateur connecté (contexte JWT)
     * @param estAdministrateur true si l'appelant est un administrateur (accès secondaire/consultation)
     */
    SimulateurTrajetDTO calculer(SimulateurTrajetRequest request, Integer idUtilisateurConnecte, boolean estAdministrateur);

    /** Compare Autoroute vs Nationale pour le même couple origine/destination, sans persister. */
    List<SimulateurTrajetDTO> comparerItineraires(SimulateurTrajetRequest request, Integer idUtilisateurConnecte, boolean estAdministrateur);

    /** Rattache une simulation déjà calculée au budget_propose d'une mission. */
    SimulateurTrajetDTO proposerBudgetMission(Integer idSimulation, Integer idMission);

    List<SimulateurTrajetDTO> historiqueParTechnicien(Integer idTechnicien);
}