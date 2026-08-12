package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "observateur_resource_assignee")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ObservateurResourceAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_observateur", nullable = false)
    private Observateur observateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ressource", nullable = false)
    private RessourceInstallation ressource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_par_admin", nullable = false)
    private Administrateur assigneParAdmin;

    @Column(name = "date_assignation", nullable = false)
    private LocalDateTime dateAssignation = LocalDateTime.now();

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
}