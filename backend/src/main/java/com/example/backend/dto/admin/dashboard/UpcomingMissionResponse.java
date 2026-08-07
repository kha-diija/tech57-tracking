package com.example.backend.dto.admin.dashboard;

public class UpcomingMissionResponse {
    private String code;
    private String title;
    private String subtitle;
    private String time;
    private String technicien;

    // Getters et Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getTechnicien() { return technicien; }
    public void setTechnicien(String technicien) { this.technicien = technicien; }
}