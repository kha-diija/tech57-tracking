package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checklist_equipement")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChecklistEquipement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_checklist")
    private Integer idChecklist;

    @Column(name = "type_checklist", nullable = false, length = 20)
    private String typeChecklist;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention", nullable = false)
    private Intervention intervention;
}