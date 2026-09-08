package com.example.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MouvementMaterielDTO {
    private Integer idMouvement;
    private String type;
    private LocalDateTime dateMouvement;
    private String origine;
    private String destination;
}