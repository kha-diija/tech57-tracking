package com.example.backend.controller;

import com.example.backend.dto.partenaire.PartenaireDashboardDto;
import com.example.backend.service.PartenaireDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partenaire")
@PreAuthorize("hasRole('PARTENAIRE')")
public class PartenaireController {

    private final PartenaireDashboardService dashboardService;

    public PartenaireController(PartenaireDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public PartenaireDashboardDto getDashboard() {
        return dashboardService.getDashboard();
    }
}