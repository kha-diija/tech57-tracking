package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_in_out")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CheckInOut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_checkinout")
    private Integer idCheckinout;

    @Column(name = "date_heure_checkin")
    private LocalDateTime dateHeureCheckin;

    @Column(name = "date_heure_checkout")
    private LocalDateTime dateHeureCheckout;

    @Column(name = "duree_minutes")
    private Integer dureeMinutes;

    @Column(name = "gps_checkin", length = 100)
    private String gpsCheckin;

    @Column(name = "gps_checkout", length = 100)
    private String gpsCheckout;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intervention", nullable = false, unique = true)
    private Intervention intervention;
}