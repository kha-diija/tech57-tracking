package com.example.backend.dto.stock.dashboard;

public class StockKpiResponse {
    private String id;
    private String label;
    private double value;
    private String suffix;
    private double trend;
    private boolean trendUp;
    private String comparison;

    // Getters / Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public double getTrend() { return trend; }
    public void setTrend(double trend) { this.trend = trend; }
    public boolean isTrendUp() { return trendUp; }
    public void setTrendUp(boolean trendUp) { this.trendUp = trendUp; }
    public String getComparison() { return comparison; }
    public void setComparison(String comparison) { this.comparison = comparison; }
}