package com.example.backend.dto.admin.intervention;

import java.time.LocalDateTime;

public class CheckInOutDto {
    private Integer idCheckinout;
    private Integer numeroVisite;
    private LocalDateTime dateHeureCheckin;
    private LocalDateTime dateHeureCheckout;
    private Integer dureeMinutes;
    private String gpsCheckin;
    private String gpsCheckout;

    public CheckInOutDto() {}

    // Getters et Setters
    public Integer getIdCheckinout() { return idCheckinout; }
    public void setIdCheckinout(Integer idCheckinout) { this.idCheckinout = idCheckinout; }

    public Integer getNumeroVisite() { return numeroVisite; }
    public void setNumeroVisite(Integer numeroVisite) { this.numeroVisite = numeroVisite; }

    public LocalDateTime getDateHeureCheckin() { return dateHeureCheckin; }
    public void setDateHeureCheckin(LocalDateTime dateHeureCheckin) { this.dateHeureCheckin = dateHeureCheckin; }

    public LocalDateTime getDateHeureCheckout() { return dateHeureCheckout; }
    public void setDateHeureCheckout(LocalDateTime dateHeureCheckout) { this.dateHeureCheckout = dateHeureCheckout; }

    public Integer getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(Integer dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getGpsCheckin() { return gpsCheckin; }
    public void setGpsCheckin(String gpsCheckin) { this.gpsCheckin = gpsCheckin; }

    public String getGpsCheckout() { return gpsCheckout; }
    public void setGpsCheckout(String gpsCheckout) { this.gpsCheckout = gpsCheckout; }
}