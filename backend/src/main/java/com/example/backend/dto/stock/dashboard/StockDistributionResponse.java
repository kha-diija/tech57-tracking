package com.example.backend.dto.stock.dashboard;

public class StockDistributionResponse {
    private String label;
    private long value;
    private String color;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}