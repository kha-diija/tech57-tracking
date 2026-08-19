package com.example.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Centralise le calcul du statut affiché d'une intervention
 * (Planifiée / En cours / En retard / Exécutée / Clôturée),
 * pour éviter de dupliquer cette logique dans plusieurs services
 * (InterventionService, TechnicienDashboardService, etc.).
 */
public class InterventionStatutHelper {

    private InterventionStatutHelper() {}

    public static String calculerStatutAffiche(Intervention intervention) {
        List<CheckInOut> visites = intervention.getCheckInOuts() != null
                ? intervention.getCheckInOuts() : new ArrayList<>();

        long visitesTerminees = visites.stream()
                .filter(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() != null)
                .count();
        boolean uneVisiteEnCours = visites.stream()
                .anyMatch(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() == null);

        double avancement = intervention.getTauxAvancement() != null ? intervention.getTauxAvancement() : 0.0;
        boolean travailCommence = !visites.isEmpty() || avancement > 0;

        String statut;
        if (!travailCommence) {
            if (intervention.getDatePrevue() != null && intervention.getDatePrevue().isBefore(LocalDateTime.now())) {
                statut = "En retard";
            } else {
                statut = "Planifiée";
            }
        } else if (visitesTerminees >= 2 && !uneVisiteEnCours) {
            statut = "Exécutée";
        } else {
            statut = "En cours";
        }

        if ("Clôturée".equals(intervention.getStatut())) {
            statut = "Clôturée";
        }
        return statut;
    }

    public static long compterVisitesTerminees(Intervention intervention) {
        List<CheckInOut> visites = intervention.getCheckInOuts() != null
                ? intervention.getCheckInOuts() : new ArrayList<>();
        return visites.stream()
                .filter(v -> v.getDateHeureCheckin() != null && v.getDateHeureCheckout() != null)
                .count();
    }
}