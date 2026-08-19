package com.example.backend.controller.stock;

import com.example.backend.dto.stock.dashboard.*;
import com.example.backend.service.stock.StockDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gestionnaire-stock/dashboard")
public class StockDashboardController {

    private final StockDashboardService stockDashboardService;

    public StockDashboardController(StockDashboardService stockDashboardService) {
        this.stockDashboardService = stockDashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<List<StockKpiResponse>> getKpis() {
        return ResponseEntity.ok(stockDashboardService.getKpis());
    }

    @GetMapping("/weekly-stock-out")
    public ResponseEntity<List<StockOutPointResponse>> getWeeklyStockOut() {
        return ResponseEntity.ok(stockDashboardService.getWeeklyStockOut());
    }

    @GetMapping("/stock-distribution")
    public ResponseEntity<List<StockDistributionResponse>> getStockDistribution() {
        return ResponseEntity.ok(stockDashboardService.getStockDistribution());
    }

    @GetMapping("/maintenance")
    public ResponseEntity<List<MaintenanceItemResponse>> getMaintenanceList() {
        return ResponseEntity.ok(stockDashboardService.getMaintenanceList());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<LowStockItemResponse>> getLowStockAlerts() {
        return ResponseEntity.ok(stockDashboardService.getLowStockAlerts());
    }
}