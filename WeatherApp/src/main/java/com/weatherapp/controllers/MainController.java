package com.weatherapp.controllers;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.weatherapp.models.WeatherData;
import com.weatherapp.services.WeatherService;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class MainController {
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Text cityName;
    @FXML private Text temperature;
    @FXML private Text weatherDescription;
    @FXML private FlowPane weatherDetailsContainer;
    @FXML private HBox forecastContainer;
    @FXML private VBox weatherInfo;
    @FXML private ScrollPane detailsScrollPane;

    private WeatherService weatherService;
    private Map<String, Image> iconCache;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        weatherService = new WeatherService();
        iconCache = new HashMap<>();
        weatherInfo.setVisible(false);
        
        // Add enter key handler for search field
        searchField.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ENTER")) {
                handleSearch();
            }
        });

        // Initialize the icons
        loadIcons();
    }

    private void loadIcons() {
        String[] iconNames = {
            "humidity", "wind", "pressure", "visibility",
            "sunrise", "sunset", "precipitation", "air-quality",
            "feels-like", "wind-direction"
        };
        
        for (String iconName : iconNames) {
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/" + iconName + ".png"));
                iconCache.put(iconName, icon);
            } catch (Exception e) {
                System.err.println("Failed to load icon: " + iconName);
            }
        }
    }

    @FXML
    private void handleSearch() {
        String city = searchField.getText().trim();
        if (city.isEmpty()) {
            showError("Please enter a city name");
            return;
        }

        searchButton.setDisable(true);
        showError("Loading weather data...");

        new Thread(() -> {
            try {
                WeatherData weatherData = weatherService.getWeatherData(city);
                Platform.runLater(() -> {
                    updateWeatherDisplay(weatherData);
                    updateForecast(city);
                    searchButton.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error: " + e.getMessage());
                    searchButton.setDisable(false);
                });
            }
        }).start();
    }

    private void updateWeatherDisplay(WeatherData data) {
        try {
            weatherInfo.getChildren().clear();
            
            // Main weather information
            VBox mainInfo = new VBox(10);
            mainInfo.setAlignment(Pos.CENTER);
            
            cityName.setText(data.getCityName());
            temperature.setText(String.format("%.1f°C", data.getTemperature()));
            weatherDescription.setText(data.getDescription());
            
            mainInfo.getChildren().addAll(cityName, temperature, weatherDescription);
            
            // Weather details in boxes
            weatherDetailsContainer.getChildren().clear();
            weatherDetailsContainer.setHgap(10);
            weatherDetailsContainer.setVgap(10);
            
            // Add weather detail boxes
            addWeatherBox("Feels Like", String.format("%.1f°C", data.getFeelsLike()), "feels-like");
            addWeatherBox("Humidity", data.getHumidity() + "%", "humidity");
            addWeatherBox("Wind", String.format("%.1f m/s", data.getWindSpeed()), "wind");
            addWeatherBox("Wind Direction", data.getWindDirection(), "wind-direction");
            addWeatherBox("Pressure", data.getPressure() + " hPa", "pressure");
            addWeatherBox("Visibility", formatVisibility(data.getVisibility()), "visibility");
            addWeatherBox("Precipitation", String.format("%.1f mm", data.getPrecipitation()), "precipitation");
            addWeatherBox("Air Quality", getAirQualityDescription(data.getAirQuality()), "air-quality");
            addWeatherBox("Sunrise", data.getSunrise().format(TIME_FORMATTER), "sunrise");
            addWeatherBox("Sunset", data.getSunset().format(TIME_FORMATTER), "sunset");

            weatherInfo.getChildren().addAll(mainInfo, weatherDetailsContainer);
            weatherInfo.setVisible(true);
        } catch (Exception e) {
            showError("Error updating display: " + e.getMessage());
        }
    }

    private void addWeatherBox(String title, String value, String iconName) {
        VBox box = new VBox(5);
        box.getStyleClass().add("weather-box");
        box.setAlignment(Pos.CENTER);

        ImageView iconView = new ImageView(iconCache.get(iconName));
        iconView.setFitWidth(32);
        iconView.setFitHeight(32);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("weather-box-title");

        Text valueText = new Text(value);
        valueText.getStyleClass().add("weather-box-value");

        box.getChildren().addAll(iconView, titleLabel, valueText);
        weatherDetailsContainer.getChildren().add(box);
    }

    private String formatVisibility(int visibility) {
        if (visibility >= 1000) {
            return String.format("%.1f km", visibility / 1000.0);
        }
        return visibility + " m";
    }

    private String getAirQualityDescription(int aqi) {
        switch (aqi) {
            case 1: return "Good";
            case 2: return "Fair";
            case 3: return "Moderate";
            case 4: return "Poor";
            case 5: return "Very Poor";
            default: return "Unknown";
        }
    }

    private void updateForecast(String city) {
        try {
            forecastContainer.getChildren().clear();
            weatherService.getForecast(city).forEach(forecast -> {
                VBox forecastCard = new VBox(5);
                forecastCard.getStyleClass().add("forecast-card");
                forecastCard.setAlignment(Pos.CENTER);

                Text date = new Text(forecast.getDate());
                Text temp = new Text(String.format("%.1f°C", forecast.getTemperature()));
                Text desc = new Text(forecast.getDescription());

                forecastCard.getChildren().addAll(date, temp, desc);
                forecastContainer.getChildren().add(forecastCard);
            });
        } catch (Exception e) {
            System.err.println("Error updating forecast: " + e.getMessage());
        }
    }

    private void showError(String message) {
        weatherInfo.getChildren().clear();
        Text errorText = new Text(message);
        errorText.getStyleClass().add("error-text");
        weatherInfo.getChildren().add(errorText);
        weatherInfo.setVisible(true);
    }
} 