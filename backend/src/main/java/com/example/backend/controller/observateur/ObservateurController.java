package com.example.backend.controller.observateur;

import com.example.backend.dto.admin.permission.*;
import com.example.backend.dto.observateur.*;
import com.example.backend.service.observateur.ObservateurSelfService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/observateur")
public class ObservateurController {

    private final ObservateurSelfService observateurSelfService;

    public ObservateurController(ObservateurSelfService observateurSelfService) {
        this.observateurSelfService = observateurSelfService;
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoAssignmentResponse>> getMesVideos(Authentication authentication) {
        return ResponseEntity.ok(observateurSelfService.getMesVideos(authentication.getName()));
    }

    @GetMapping("/resources")
    public ResponseEntity<List<ResourceAssignmentResponse>> getMesRessources(Authentication authentication) {
        return ResponseEntity.ok(observateurSelfService.getMesRessources(authentication.getName()));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentAssignmentResponse>> getMesDocuments(Authentication authentication) {
        return ResponseEntity.ok(observateurSelfService.getMesDocuments(authentication.getName()));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ObservateurDashboardSummaryDto> getSummary(Authentication authentication) {
        return ResponseEntity.ok(observateurSelfService.getSummary(authentication.getName()));
    }

    @GetMapping("/dashboard/distribution")
    public ResponseEntity<List<DistributionItemDto>> getDistribution(Authentication authentication) {
        return ResponseEntity.ok(observateurSelfService.getDistribution(authentication.getName()));
    }

    @GetMapping("/dashboard/timeline")
    public ResponseEntity<List<TimelinePointDto>> getTimeline(Authentication authentication) {
        return ResponseEntity.ok(observateurSelfService.getTimeline(authentication.getName()));
    }
}