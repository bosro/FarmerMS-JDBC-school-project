package com.example.jdbc_assignment.models;

public class CropWeatherRecommendation {
    private String crop;
    private double minTemperature;
    private double maxTemperature;
    private double optimalTemperature;
    private double minHumidity;
    private double maxHumidity;
    private double optimalHumidity;
    private String plantingSeason;
    private String harvestSeason;
    private String tips;

    // Constructor
    public CropWeatherRecommendation(String crop, double minTemperature, double maxTemperature,
                                     double optimalTemperature, double minHumidity, double maxHumidity,
                                     double optimalHumidity, String plantingSeason, String harvestSeason,
                                     String tips) {
        this.crop = crop;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.optimalTemperature = optimalTemperature;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.optimalHumidity = optimalHumidity;
        this.plantingSeason = plantingSeason;
        this.harvestSeason = harvestSeason;
        this.tips = tips;
    }

    // Getters and Setters
    public String getCrop() {
        return crop;
    }

    public void setCrop(String crop) {
        this.crop = crop;
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

    public double getOptimalTemperature() {
        return optimalTemperature;
    }

    public void setOptimalTemperature(double optimalTemperature) {
        this.optimalTemperature = optimalTemperature;
    }

    public double getMinHumidity() {
        return minHumidity;
    }

    public void setMinHumidity(double minHumidity) {
        this.minHumidity = minHumidity;
    }

    public double getMaxHumidity() {
        return maxHumidity;
    }

    public void setMaxHumidity(double maxHumidity) {
        this.maxHumidity = maxHumidity;
    }

    public double getOptimalHumidity() {
        return optimalHumidity;
    }

    public void setOptimalHumidity(double optimalHumidity) {
        this.optimalHumidity = optimalHumidity;
    }

    public String getPlantingSeason() {
        return plantingSeason;
    }

    public void setPlantingSeason(String plantingSeason) {
        this.plantingSeason = plantingSeason;
    }

    public String getHarvestSeason() {
        return harvestSeason;
    }

    public void setHarvestSeason(String harvestSeason) {
        this.harvestSeason = harvestSeason;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}