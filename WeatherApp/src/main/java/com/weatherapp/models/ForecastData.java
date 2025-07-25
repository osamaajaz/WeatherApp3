package com.weatherapp.models;

public class ForecastData {
    private String date;
    private double temperature;
    private String description;

    public ForecastData(String date, double temperature, String description) {
        this.date = date;
        this.temperature = temperature;
        this.description = description;
    }

    // Getters
    public String getDate() { return date; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
} 