package com.weatherapp.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherData {
    private double temperature;
    private double feelsLike;
    private String description;
    private int humidity;
    private double windSpeed;
    private int pressure;
    private int visibility;
    private long sunrise;
    private long sunset;
    private double precipitation;
    private int airQuality;
    private int windDirection;
    private String cityName;
    private List<Map<String, Object>> forecast;

    public WeatherData(double temperature, double feelsLike, String description, int humidity,
                      double windSpeed, int pressure, int visibility, long sunrise, long sunset,
                      double precipitation, int airQuality, int windDirection, String cityName) {
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.visibility = visibility;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.precipitation = precipitation;
        this.airQuality = airQuality;
        this.windDirection = windDirection;
        this.cityName = cityName;
        this.forecast = new ArrayList<>();
    }

    // Getters
    public double getTemperature() { return temperature; }
    public double getFeelsLike() { return feelsLike; }
    public String getDescription() { return description; }
    public int getHumidity() { return humidity; }
    public double getWindSpeed() { return windSpeed; }
    public int getPressure() { return pressure; }
    public int getVisibility() { return visibility; }
    public long getSunrise() { return sunrise; }
    public long getSunset() { return sunset; }
    public double getPrecipitation() { return precipitation; }
    public int getAirQuality() { return airQuality; }
    public int getWindDirection() { return windDirection; }
    public String getCityName() { return cityName; }
    public List<Map<String, Object>> getForecast() { return forecast; }
    
    // Setter for forecast
    public void setForecast(List<Map<String, Object>> forecast) {
        this.forecast = forecast;
    }
} 