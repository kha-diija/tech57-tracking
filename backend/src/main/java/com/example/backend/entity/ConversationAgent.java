package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_agent")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConversationAgent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversation")
    private Long idConversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 20)
    private String canal = "ACCUEIL";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String reponse;

    @Column(name = "sources_utilisees", columnDefinition = "jsonb")
    private String sourcesUtilisees;

    @Column(name = "date_question", nullable = false)
    private LocalDateTime dateQuestion = LocalDateTime.now();

    @Column(name = "latence_ms")
    private Integer latenceMs;
}
