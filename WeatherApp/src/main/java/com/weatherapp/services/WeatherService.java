package com.weatherapp.services;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weatherapp.models.ForecastData;
import com.weatherapp.models.WeatherData;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherService {
    private final String API_KEY = "0383bed93d92bd3853b5f423a72b2d84";
    private final String BASE_URL = "https://api.openweathermap.org/data/2.5";
    private final OkHttpClient client;
    private final ObjectMapper mapper;

    public WeatherService() {
        this.client = new OkHttpClient();
        this.mapper = new ObjectMapper();
    }

    public WeatherData getWeatherData(String city) throws IOException {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        // Get basic weather data
        String weatherUrl = String.format("%s/weather?q=%s&units=metric&appid=%s",
                BASE_URL, city, API_KEY);

        JsonNode weatherData = fetchJsonData(weatherUrl);
        
        // Get air quality data
        JsonNode coord = weatherData.path("coord");
        String airQualityUrl = String.format("%s/air_pollution?lat=%s&lon=%s&appid=%s",
                BASE_URL, coord.path("lat").asText(), coord.path("lon").asText(), API_KEY);
        
        JsonNode airQualityData = fetchJsonData(airQualityUrl);

        // Process weather data
        JsonNode main = weatherData.path("main");
        JsonNode weather = weatherData.path("weather").get(0);
        JsonNode wind = weatherData.path("wind");
        JsonNode sys = weatherData.path("sys");

        // Convert sunrise and sunset timestamps to LocalDateTime
        LocalDateTime sunrise = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(sys.path("sunrise").asLong()),
            ZoneId.systemDefault()
        );
        
        LocalDateTime sunset = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(sys.path("sunset").asLong()),
            ZoneId.systemDefault()
        );

        // Get wind direction
        String windDirection = getWindDirection(wind.path("deg").asDouble());

        // Get precipitation (if available)
        double precipitation = 0.0;
        if (weatherData.has("rain")) {
            precipitation = weatherData.path("rain").path("1h").asDouble(0.0);
        } else if (weatherData.has("snow")) {
            precipitation = weatherData.path("snow").path("1h").asDouble(0.0);
        }

        // Get air quality index
        int aqi = airQualityData.path("list").get(0).path("main").path("aqi").asInt(1);

        return new WeatherData(
            weatherData.path("name").asText("Unknown"),
            main.path("temp").asDouble(0.0),
            weather.path("description").asText("No description available"),
            main.path("humidity").asInt(0),
            wind.path("speed").asDouble(0.0),
            main.path("pressure").asInt(0),
            weatherData.path("visibility").asInt(0),
            sunrise,
            sunset,
            precipitation,
            weather.path("icon").asText("01d"),
            aqi,
            main.path("feels_like").asDouble(0.0),
            windDirection
        );
    }

    private JsonNode fetchJsonData(String url) throws IOException {
        System.out.println("Fetching data from: " + url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            System.out.println("Response received: " + responseBody);

            if (!response.isSuccessful()) {
                JsonNode errorNode = mapper.readTree(responseBody);
                String errorMessage = errorNode.path("message").asText("Unknown error");
                throw new IOException("API Error: " + errorMessage + " (Code: " + response.code() + ")");
            }

            return mapper.readTree(responseBody);
        }
    }

    private String getWindDirection(double degrees) {
        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                             "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};
        int index = (int) Math.round(((degrees % 360) / 22.5));
        return directions[index % 16];
    }

    public List<ForecastData> getForecast(String city) throws IOException {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        String url = String.format("%s/forecast?q=%s&units=metric&appid=%s",
                BASE_URL, city, API_KEY);

        JsonNode root = fetchJsonData(url);
        List<ForecastData> forecast = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        if (!root.has("list")) {
            throw new IOException("Invalid forecast response format from API");
        }

        for (JsonNode item : root.path("list")) {
            try {
                String dtTxt = item.path("dt_txt").asText();
                if (dtTxt.contains("12:00:00")) {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(item.path("dt").asLong()),
                        ZoneId.systemDefault()
                    );
                    
                    forecast.add(new ForecastData(
                        dateTime.format(formatter),
                        item.path("main").path("temp").asDouble(0.0),
                        item.path("weather").get(0).path("description").asText("No description available")
                    ));
                }
            } catch (Exception e) {
                System.err.println("Error processing forecast item: " + e.getMessage());
            }
        }
        return forecast;
    }
} 