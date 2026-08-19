package com.example.backend.controller.technicien;

import com.example.backend.dto.technicien.Dashboard.TechnicienKpiResponseDTO;
import com.example.backend.service.technicien.TechnicienDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/technicien/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TECHNICIEN')")
public class TechnicienDashboardController {

    private final TechnicienDashboardService dashboardService;

    @GetMapping("/kpi")
    public ResponseEntity<TechnicienKpiResponseDTO> obtenirKpisTechnicien(Principal principal) {
        String emailTechnicien = principal.getName();
        TechnicienKpiResponseDTO kpis = dashboardService.calculerKpisPourTechnicien(emailTechnicien);
        return ResponseEntity.ok(kpis);
    }
}