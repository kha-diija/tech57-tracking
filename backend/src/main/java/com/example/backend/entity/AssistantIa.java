package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "assistant_ia")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AssistantIa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assistant")
    private Integer idAssistant;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String modele;

    @Column(columnDefinition = "TEXT")
    private String description;
}