package com.weatherapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import com.weatherapp.services.WeatherService;
import com.weatherapp.models.WeatherData;
import javafx.application.Platform;
import javafx.geometry.Pos;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

public class MainController {
    private Map<String, String> weatherIcons;
    private WeatherService weatherService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd");
    
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private VBox weatherInfo;
    @FXML
    private FlowPane detailsContainer;
    @FXML
    private FlowPane forecastContainer;
    @FXML
    private Label cityLabel;
    @FXML
    private Label temperatureLabel;
    @FXML
    private Label descriptionLabel;
    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        weatherService = new WeatherService();
        loadIcons();
        setupSearchField();
    }

    private void loadIcons() {
        weatherIcons = new HashMap<>();
        // Using Unicode symbols as temporary icons
        weatherIcons.put("humidity", "💧");
        weatherIcons.put("wind", "🌪");
        weatherIcons.put("pressure", "⭕");
        weatherIcons.put("visibility", "👁");
        weatherIcons.put("sunrise", "🌅");
        weatherIcons.put("sunset", "🌇");
        weatherIcons.put("precipitation", "🌧");
        weatherIcons.put("air-quality", "🌬");
        weatherIcons.put("feels-like", "🌡");
        weatherIcons.put("wind-direction", "🧭");
        
        // Weather condition icons
        weatherIcons.put("clear", "☀️");
        weatherIcons.put("clouds", "☁️");
        weatherIcons.put("rain", "🌧");
        weatherIcons.put("snow", "❄️");
        weatherIcons.put("thunderstorm", "⛈");
        weatherIcons.put("drizzle", "🌦");
        weatherIcons.put("mist", "🌫");
    }

    private void setupSearchField() {
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSearch();
            }
        });
    }

    @FXML
    private void handleSearch() {
        String city = searchField.getText().trim();
        if (city.isEmpty()) {
            showError("Please enter a city name");
            return;
        }

        searchButton.setDisable(true);
        clearWeatherInfo();
        showLoading();

        new Thread(() -> {
            try {
                WeatherData data = weatherService.getWeatherData(city);
                Platform.runLater(() -> {
                    updateWeatherDisplay(data);
                    updateForecast(data.getForecast());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> searchButton.setDisable(false));
            }
        }).start();
    }

    private void updateWeatherDisplay(WeatherData data) {
        clearWeatherInfo();
        
        // Update main weather information
        cityLabel.setText(data.getCityName());
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature()));
        descriptionLabel.setText(capitalizeFirst(data.getDescription()));

        // Create weather detail boxes
        detailsContainer.getChildren().clear();
        
        addWeatherBox("Feels Like", String.format("%.1f°C", data.getFeelsLike()), "feels-like");
        addWeatherBox("Humidity", data.getHumidity() + "%", "humidity");
        addWeatherBox("Wind", String.format("%.1f m/s", data.getWindSpeed()), "wind");
        addWeatherBox("Pressure", data.getPressure() + " hPa", "pressure");
        addWeatherBox("Visibility", formatVisibility(data.getVisibility()), "visibility");
        addWeatherBox("Sunrise", formatTime(data.getSunrise()), "sunrise");
        addWeatherBox("Sunset", formatTime(data.getSunset()), "sunset");
        addWeatherBox("Precipitation", String.format("%.1f mm", data.getPrecipitation()), "precipitation");
        addWeatherBox("Air Quality", getAirQualityDescription(data.getAirQuality()), "air-quality");
        addWeatherBox("Wind Direction", String.format("%d°", data.getWindDirection()), "wind-direction");
    }
    
    private void updateForecast(List<Map<String, Object>> forecast) {
        forecastContainer.getChildren().clear();
        
        if (forecast == null || forecast.isEmpty()) {
            System.out.println("No forecast data available");
            return;
        }
        
        System.out.println("Received forecast data: " + forecast.size() + " days");
        
        for (Map<String, Object> day : forecast) {
            try {
                VBox forecastCard = new VBox(5);
                forecastCard.getStyleClass().add("forecast-card");
                forecastCard.setAlignment(Pos.CENTER);
                
                // Add date
                String date = (String) day.get("date");
                System.out.println("Processing forecast for date: " + date);
                LocalDateTime dateTime = LocalDateTime.parse(date + "T12:00:00");
                Label dateLabel = new Label(dateTime.format(dateFormatter));
                dateLabel.getStyleClass().add("weather-box-title");
                
                // Add icon based on weather description
                String description = (String) day.get("description");
                String iconKey = getIconKeyFromDescription(description);
                Label iconLabel = new Label(weatherIcons.get(iconKey));
                iconLabel.getStyleClass().add("weather-icon");
                
                // Add temperature
                double temp = (double) day.get("temp");
                Label tempLabel = new Label(String.format("%.1f°C", temp));
                tempLabel.getStyleClass().add("weather-box-value");
                
                // Add description
                Label descLabel = new Label(capitalizeFirst(description));
                descLabel.getStyleClass().add("weather-box-title");
                descLabel.setWrapText(true);
                
                forecastCard.getChildren().addAll(dateLabel, iconLabel, tempLabel, descLabel);
                forecastContainer.getChildren().add(forecastCard);
                System.out.println("Added forecast card for: " + date);
            } catch (Exception e) {
                System.err.println("Error creating forecast card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private String getIconKeyFromDescription(String description) {
        description = description.toLowerCase();
        if (description.contains("clear")) return "clear";
        if (description.contains("cloud")) return "clouds";
        if (description.contains("rain")) return "rain";
        if (description.contains("snow")) return "snow";
        if (description.contains("thunder")) return "thunderstorm";
        if (description.contains("drizzle")) return "drizzle";
        if (description.contains("mist") || description.contains("fog")) return "mist";
        return "clouds"; // Default
    }

    private void addWeatherBox(String title, String value, String iconKey) {
        VBox box = new VBox(5);
        box.getStyleClass().add("weather-box");
        box.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(weatherIcons.get(iconKey));
        iconLabel.getStyleClass().add("weather-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("weather-box-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("weather-box-value");

        box.getChildren().addAll(iconLabel, titleLabel, valueLabel);
        detailsContainer.getChildren().add(box);
    }

    private String formatTime(long timestamp) {
        LocalDateTime time = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp),
            ZoneId.systemDefault()
        );
        return time.format(timeFormatter);
    }

    private String formatVisibility(int visibility) {
        return visibility >= 1000 
            ? String.format("%.1f km", visibility / 1000.0)
            : visibility + " m";
    }

    private String getAirQualityDescription(int aqi) {
        return switch (aqi) {
            case 1 -> "Good";
            case 2 -> "Fair";
            case 3 -> "Moderate";
            case 4 -> "Poor";
            case 5 -> "Very Poor";
            default -> "Unknown";
        };
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showLoading() {
        errorLabel.setText("Loading...");
        errorLabel.setVisible(true);
    }

    private void clearWeatherInfo() {
        errorLabel.setVisible(false);
        detailsContainer.getChildren().clear();
        forecastContainer.getChildren().clear();
    }
} 