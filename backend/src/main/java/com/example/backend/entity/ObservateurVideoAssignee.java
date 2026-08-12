package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "observateur_video_assignee")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ObservateurVideoAssignee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_observateur", nullable = false)
    private Observateur observateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_video", nullable = false)
    private VideoMateriel video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigne_par_admin", nullable = false)
    private Administrateur assigneParAdmin;

    @Column(name = "date_assignation", nullable = false)
    private LocalDateTime dateAssignation = LocalDateTime.now();

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
}