package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_materiel")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockMateriel {

    @Id
    @Column(name = "id_materiel")
    private Integer idMateriel;

    // Partage la même PK que Materiel (relation 1-1 par id_materiel)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id_materiel")
    private Materiel materiel;

    @Column(name = "quantite_disponible", nullable = false)
    private Integer quantiteDisponible = 0;

    @Column(name = "quantite_reservee", nullable = false)
    private Integer quantiteReservee = 0;

    @Column(name = "quantite_en_panne", nullable = false)
    private Integer quantiteEnPanne = 0;

    @Column(name = "seuil_alerte")
    private Integer seuilAlerte = 0;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        dateMaj = LocalDateTime.now();
    }
}