package com.example.backend.controller.admin;

import com.example.backend.dto.admin.dashboard.*;
import com.example.backend.service.admin.DashboardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/kpis")
    public ResponseEntity<List<KpiResponse>> getKpis(
            @RequestParam(name = "period", defaultValue = "7d") String period) {
        return ResponseEntity.ok(dashboardService.getKpis(period));
    }

    @GetMapping("/weekly-missions")
    public ResponseEntity<List<WeeklyMissionResponse>> getWeeklyMissions() {
        return ResponseEntity.ok(dashboardService.getWeeklyMissions());
    }

    @GetMapping("/installation-progress")
    public ResponseEntity<List<InstallationPointResponse>> getInstallationProgress() {
        return ResponseEntity.ok(dashboardService.getInstallationProgress());
    }

    @GetMapping("/material-distribution")
    public ResponseEntity<List<MaterialDistributionResponse>> getMaterialDistribution() {
        return ResponseEntity.ok(dashboardService.getMaterialDistribution());
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<List<ActivityItemResponse>> getRecentActivity() {
        return ResponseEntity.ok(dashboardService.getRecentActivity());
    }

    @GetMapping("/upcoming-missions")
    public ResponseEntity<List<UpcomingMissionResponse>> getUpcomingMissions() {
        return ResponseEntity.ok(dashboardService.getUpcomingMissions());
    }

    // --- Endpoints pour les actions du Frontend ---

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDashboard() {
        byte[] data = dashboardService.exportDashboardData();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=dashboard-report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }


}