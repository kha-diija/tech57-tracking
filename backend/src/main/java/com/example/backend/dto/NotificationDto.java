package com.example.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationDto {
    private Integer idNotification;
    private String message;
    private LocalDateTime dateEnvoi;
    private Boolean lu;
    private String type;
    private String expediteurNom;
}