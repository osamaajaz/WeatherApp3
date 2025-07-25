package com.weatherapp.services;

import com.weatherapp.models.WeatherData;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class WeatherService {
    private static final String API_KEY = "0383bed93d92bd3853b5f423a72b2d84";
    private static final String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String AIR_QUALITY_URL = "https://api.openweathermap.org/data/2.5/air_pollution";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private final HttpClient client;
    private static final Logger logger = Logger.getLogger(WeatherService.class.getName());

    public WeatherService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public WeatherData getWeatherData(String city) throws Exception {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City name cannot be empty");
        }

        // Fetch basic weather data
        String weatherUrl = String.format("%s?q=%s&units=metric&appid=%s", WEATHER_URL, city, API_KEY);
        JSONObject weatherData = fetchJsonData(weatherUrl);
        
        // Extract coordinates for air quality data
        double lat = weatherData.getJSONObject("coord").getDouble("lat");
        double lon = weatherData.getJSONObject("coord").getDouble("lon");
        
        // Fetch air quality data
        String airQualityUrl = String.format("%s?lat=%f&lon=%f&appid=%s", AIR_QUALITY_URL, lat, lon, API_KEY);
        JSONObject airQualityData = fetchJsonData(airQualityUrl);
        
        // Fetch 5-day forecast data
        String forecastUrl = String.format("%s?q=%s&units=metric&appid=%s", FORECAST_URL, city, API_KEY);
        JSONObject forecastData = fetchJsonData(forecastUrl);
        List<Map<String, Object>> forecast = parseForecastData(forecastData);

        // Extract weather information
        JSONObject main = weatherData.getJSONObject("main");
        JSONObject weather = weatherData.getJSONArray("weather").getJSONObject(0);
        JSONObject wind = weatherData.getJSONObject("wind");
        JSONObject sys = weatherData.getJSONObject("sys");
        
        // Get air quality index
        int aqi = airQualityData.getJSONArray("list")
                .getJSONObject(0)
                .getJSONObject("main")
                .getInt("aqi");

        // Create and return WeatherData object with forecast
        WeatherData data = new WeatherData(
            main.getDouble("temp"),
            main.getDouble("feels_like"),
            weather.getString("description"),
            main.getInt("humidity"),
            wind.getDouble("speed"),
            main.getInt("pressure"),
            weatherData.getInt("visibility"),
            sys.getLong("sunrise"),
            sys.getLong("sunset"),
            weatherData.has("rain") ? weatherData.getJSONObject("rain").optDouble("1h", 0.0) : 0.0,
            aqi,
            wind.getInt("deg"),
            weatherData.getString("name")
        );
        
        // Add forecast data
        data.setForecast(forecast);
        
        return data;
    }
    
    private List<Map<String, Object>> parseForecastData(JSONObject forecastData) {
        List<Map<String, Object>> forecastList = new ArrayList<>();
        JSONArray list = forecastData.getJSONArray("list");
        
        // Get one forecast per day (every 8th item is noon of each day)
        Map<String, Boolean> daysAdded = new HashMap<>();
        
        System.out.println("Parsing forecast data with " + list.length() + " entries");
        
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            String dateText = item.getString("dt_txt").split(" ")[0]; // Get just the date
            
            // Only add one forecast per day
            if (!daysAdded.containsKey(dateText)) {
                Map<String, Object> dayForecast = new HashMap<>();
                
                JSONObject main = item.getJSONObject("main");
                JSONObject weather = item.getJSONArray("weather").getJSONObject(0);
                
                dayForecast.put("date", dateText);
                dayForecast.put("temp", main.getDouble("temp"));
                dayForecast.put("description", weather.getString("description"));
                dayForecast.put("icon", weather.getString("icon"));
                
                forecastList.add(dayForecast);
                daysAdded.put(dateText, true);
                
                System.out.println("Added forecast for date: " + dateText + 
                                  ", temp: " + main.getDouble("temp") + 
                                  ", description: " + weather.getString("description"));
                
                // Stop after we have 5 days
                if (forecastList.size() >= 5) {
                    break;
                }
            }
        }
        
        System.out.println("Final forecast list has " + forecastList.size() + " days");
        return forecastList;
    }

    private JSONObject fetchJsonData(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        logger.log(Level.INFO, "Fetching data from: " + url);
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            logger.log(Level.SEVERE, "Error response: " + response.body());
            throw new RuntimeException("Failed to fetch data: HTTP " + response.statusCode());
        }

        logger.log(Level.INFO, "Response received: " + response.body());
        return new JSONObject(response.body());
    }
} 