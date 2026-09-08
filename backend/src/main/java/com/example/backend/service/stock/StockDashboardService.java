package com.example.backend.service.stock;

import com.example.backend.dto.stock.dashboard.*;
import com.example.backend.entity.Materiel;
import com.example.backend.entity.StockMateriel;
import com.example.backend.repository.stock.MaterielDashboardRepository;
import com.example.backend.repository.stock.SortieDashboardRepository;
import com.example.backend.repository.stock.StockDashboardRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class StockDashboardService {

    private static final List<String> ETATS_MAINTENANCE = List.of("En panne", "En maintenance", "À réparer");

    private final StockDashboardRepository stockRepository;
    private final SortieDashboardRepository sortieRepository;
    private final MaterielDashboardRepository materielRepository;

    public StockDashboardService(StockDashboardRepository stockRepository,
                                 SortieDashboardRepository sortieRepository,
                                 MaterielDashboardRepository materielRepository) {
        this.stockRepository = stockRepository;
        this.sortieRepository = sortieRepository;
        this.materielRepository = materielRepository;
    }

    public List<StockKpiResponse> getKpis() {
        long disponible = stockRepository.sumQuantiteDisponible();
        long reservee = stockRepository.sumQuantiteReservee();
        long enPanne = stockRepository.sumQuantiteEnPanne();
        long total = disponible + reservee + enPanne;

        YearMonth moisCourant = YearMonth.now();
        long sortiCeMois = sortieRepository.sumQuantiteSortiePeriode(
                moisCourant.atDay(1).atStartOfDay(),
                moisCourant.atEndOfMonth().atTime(23, 59, 59));

        YearMonth moisPrecedent = moisCourant.minusMonths(1);
        long sortiMoisPrecedent = sortieRepository.sumQuantiteSortiePeriode(
                moisPrecedent.atDay(1).atStartOfDay(),
                moisPrecedent.atEndOfMonth().atTime(23, 59, 59));

        double trendSortie = sortiMoisPrecedent == 0 ? 0
                : Math.round(((sortiCeMois - sortiMoisPrecedent) * 100.0 / sortiMoisPrecedent) * 10) / 10.0;

        long enMaintenance = materielRepository.countByEtatIn(ETATS_MAINTENANCE);
        double tauxDisponibilite = total == 0 ? 0 : Math.round((disponible * 1000.0 / total)) / 10.0;

        StockKpiResponse kpi1 = new StockKpiResponse();
        kpi1.setId("sorties");
        kpi1.setLabel("Matériel sorti ce mois");
        kpi1.setValue(sortiCeMois);
        kpi1.setTrend(Math.abs(trendSortie));
        kpi1.setTrendUp(trendSortie >= 0);
        kpi1.setComparison("vs mois dernier");

        StockKpiResponse kpi2 = new StockKpiResponse();
        kpi2.setId("disponible");
        kpi2.setLabel("Matériel disponible");
        kpi2.setValue(disponible);
        kpi2.setComparison("prêt à être affecté");

        StockKpiResponse kpi3 = new StockKpiResponse();
        kpi3.setId("maintenance");
        kpi3.setLabel("Matériel en maintenance");
        kpi3.setValue(enMaintenance);
        kpi3.setComparison("nécessite une intervention");

        StockKpiResponse kpi4 = new StockKpiResponse();
        kpi4.setId("disponibilite");
        kpi4.setLabel("Taux de disponibilité");
        kpi4.setValue(tauxDisponibilite);
        kpi4.setSuffix("%");
        kpi4.setComparison("du parc total");

        return List.of(kpi1, kpi2, kpi3, kpi4);
    }

    /** Sorties de stock sur les 4 dernières semaines (lundi → dimanche), pour le graphique en barres. */
    public List<StockOutPointResponse> getWeeklyStockOut() {
        List<StockOutPointResponse> points = new ArrayList<>();

        LocalDate lundiCourant = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        for (int i = 3; i >= 0; i--) {
            LocalDate debutSemaine = lundiCourant.minusWeeks(i);
            LocalDate finSemaine = debutSemaine.plusDays(6);

            LocalDateTime debut = debutSemaine.atStartOfDay();
            LocalDateTime fin = finSemaine.atTime(23, 59, 59);

            long total = sortieRepository.sumQuantiteSortiePeriode(debut, fin);

            StockOutPointResponse point = new StockOutPointResponse();
            point.setPeriode(i == 0 ? "Cette sem." : "S-" + i);
            point.setQuantite(total);
            points.add(point);
        }
        return points;
    }

    /** Répartition disponible / réservé / en panne, pour le donut. */
    public List<StockDistributionResponse> getStockDistribution() {
        StockDistributionResponse d1 = new StockDistributionResponse();
        d1.setLabel("Disponible");
        d1.setValue(stockRepository.sumQuantiteDisponible());
        d1.setColor("#22c55e");

        StockDistributionResponse d2 = new StockDistributionResponse();
        d2.setLabel("Réservé");
        d2.setValue(stockRepository.sumQuantiteReservee());
        d2.setColor("#e85002");

        StockDistributionResponse d3 = new StockDistributionResponse();
        d3.setLabel("En panne");
        d3.setValue(stockRepository.sumQuantiteEnPanne());
        d3.setColor("#ef4444");

        return List.of(d1, d2, d3);
    }

    /** Liste du matériel nécessitant une maintenance. */
    public List<MaintenanceItemResponse> getMaintenanceList() {
        List<Materiel> materiels = materielRepository.findByEtatIn(ETATS_MAINTENANCE);
        List<MaintenanceItemResponse> result = new ArrayList<>();

        for (Materiel m : materiels) {
            MaintenanceItemResponse item = new MaintenanceItemResponse();
            item.setIdMateriel(m.getIdMateriel());
            item.setReference(m.getReference());
            item.setNom(m.getNom());
            item.setEtat(m.getEtat());
            item.setCategorie(m.getCategorie() != null ? m.getCategorie().getNom() : "—");
            item.setEtablissement(m.getEtablissement() != null ? m.getEtablissement().toString() : "—");
            result.add(item);
        }
        return result;
    }

    /** Matériel dont la quantité disponible est sous le seuil d'alerte. */
    public List<LowStockItemResponse> getLowStockAlerts() {
        List<StockMateriel> stocksBas = stockRepository.findStockBas();
        List<LowStockItemResponse> result = new ArrayList<>();

        for (StockMateriel s : stocksBas) {
            LowStockItemResponse item = new LowStockItemResponse();
            item.setIdMateriel(s.getIdMateriel());
            item.setNom(s.getMateriel() != null ? s.getMateriel().getNom() : "—");
            item.setReference(s.getMateriel() != null ? s.getMateriel().getReference() : "—");
            item.setQuantiteDisponible(s.getQuantiteDisponible());
            item.setSeuilAlerte(s.getSeuilAlerte() != null ? s.getSeuilAlerte() : 0);
            result.add(item);
        }
        return result;
    }
}