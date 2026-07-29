package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MaintenanceRequest {

    @NotNull(message = "La date de maintenance est obligatoire")
    private LocalDate dateMaintenance;

    private String description;

    private Double cout;

    // false par défaut : le matériel passe "indisponible / en réparation"
    // dès l'ouverture de la maintenance. Repasser à true quand il est rendu.
    private Boolean disponible = false;

    @NotNull(message = "Le matériel est obligatoire")
    private Integer idMateriel;
}