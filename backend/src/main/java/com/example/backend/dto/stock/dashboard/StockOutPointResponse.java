package com.example.backend.dto.stock.dashboard;

public class StockOutPointResponse {
    private String periode; // ex: "Avr", "Mai"...
    private long quantite;

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }
    public long getQuantite() { return quantite; }
    public void setQuantite(long quantite) { this.quantite = quantite; }
}