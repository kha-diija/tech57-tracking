package com.example.backend.dto.admin.dashboard;

public class WeeklyMissionResponse {
    private String day;
    private int planned;
    private int completed;

    // Getters et Setters
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public int getPlanned() { return planned; }
    public void setPlanned(int planned) { this.planned = planned; }
    public int getCompleted() { return completed; }
    public void setCompleted(int completed) { this.completed = completed; }
}