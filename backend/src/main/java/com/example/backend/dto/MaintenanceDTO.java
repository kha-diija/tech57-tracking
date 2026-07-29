package com.example.backend.dto;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceDTO {
    private Integer idMaintenance;
    private LocalDate dateMaintenance;
    private String description;
    private Double cout;
    private Boolean disponible;
    private Integer idMateriel;
    private String referenceMateriel;
}