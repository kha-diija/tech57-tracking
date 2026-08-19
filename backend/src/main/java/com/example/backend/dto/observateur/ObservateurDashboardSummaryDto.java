package com.example.backend.dto.observateur;

import java.time.LocalDateTime;

public record ObservateurDashboardSummaryDto(
        int totalVideos,
        int totalRessources,
        int totalDocuments,
        int totalElements,
        LocalDateTime dernierAssignation
) {}