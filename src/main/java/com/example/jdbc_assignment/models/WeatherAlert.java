package com.example.jdbc_assignment.models;

import java.time.LocalDateTime;

public class WeatherAlert {
    private String title;
    private String description;
    private String type; // e.g., rain, heat, cold, wind
    private String severity; // severe, moderate, minor
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public WeatherAlert(String title, String description, String type,
                        String severity, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.severity = severity;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}