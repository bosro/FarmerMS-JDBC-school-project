package com.example.jdbc_assignment.models;

import java.time.LocalDateTime;

public class WeatherForecast {
    private LocalDateTime date;
    private String description;
    private double temperature;
    private double minTemperature;
    private double maxTemperature;
    private double humidity;
    private double windSpeed;
    private String icon;

    // Default constructor
    public WeatherForecast() {}

    // Constructor with parameters
    public WeatherForecast(LocalDateTime date, String description, double temperature,
                           double minTemperature, double maxTemperature, double humidity,
                           double windSpeed, String icon) {
        this.date = date;
        this.description = description;
        this.temperature = temperature;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.icon = icon;
    }

    // Getters and Setters
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}