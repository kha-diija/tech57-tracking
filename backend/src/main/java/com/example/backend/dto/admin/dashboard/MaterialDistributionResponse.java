package com.example.backend.dto.admin.dashboard;

public class MaterialDistributionResponse {
    private String label;
    private int value;
    private String color;

    // Getters et Setters
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}