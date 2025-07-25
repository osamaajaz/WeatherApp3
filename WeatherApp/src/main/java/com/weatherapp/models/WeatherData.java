package com.weatherapp.models;

import java.time.LocalDateTime;

public class WeatherData {
    private String cityName;
    private double temperature;
    private String description;
    private int humidity;
    private double windSpeed;
    private int pressure;
    private int visibility;
    private LocalDateTime sunrise;
    private LocalDateTime sunset;
    private double precipitation;
    private String icon;
    private int airQuality;
    private double feelsLike;
    private String windDirection;

    public WeatherData(String cityName, double temperature, String description, 
                      int humidity, double windSpeed, int pressure,
                      int visibility, LocalDateTime sunrise, LocalDateTime sunset,
                      double precipitation, String icon, int airQuality,
                      double feelsLike, String windDirection) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.visibility = visibility;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.precipitation = precipitation;
        this.icon = icon;
        this.airQuality = airQuality;
        this.feelsLike = feelsLike;
        this.windDirection = windDirection;
    }

    // Getters
    public String getCityName() { return cityName; }
    public double getTemperature() { return temperature; }
    public String getDescription() { return description; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public int getPressure() { return pressure; }
    public int getVisibility() { return visibility; }
    public LocalDateTime getSunrise() { return sunrise; }
    public LocalDateTime getSunset() { return sunset; }
    public double getPrecipitation() { return precipitation; }
    public String getIcon() { return icon; }
    public int getAirQuality() { return airQuality; }
    public double getFeelsLike() { return feelsLike; }
    public String getWindDirection() { return windDirection; }
} 