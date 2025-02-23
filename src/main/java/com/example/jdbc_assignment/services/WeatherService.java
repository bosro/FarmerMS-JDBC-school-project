package com.example.jdbc_assignment.services;

import com.example.jdbc_assignment.models.Weather;
import com.example.jdbc_assignment.models.WeatherForecast;
import com.example.jdbc_assignment.models.CropWeatherRecommendation;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherService {
    private static final String API_KEY = "3c7559f67e4b724dfd19a499341ea1ba"; // Replace with your OpenWeatherMap API key
    private static final String CURRENT_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";

    // Cache for crop recommendations
    private static final Map<String, CropWeatherRecommendation> cropRecommendations = new HashMap<>();

    static {
        // Initialize crop recommendations (these are sample values; you should replace with accurate data)
        initializeCropRecommendations();
    }

    private static void initializeCropRecommendations() {
        // Maize
        cropRecommendations.put("maize", new CropWeatherRecommendation(
                "Maize", 10, 32, 25, 40, 80, 65, "April-June", "December-February",
                "Maize requires good soil moisture but is susceptible to waterlogging. Plant after the first rains."
        ));

        // Rice
        cropRecommendations.put("rice", new CropWeatherRecommendation(
                "Rice", 20, 35, 30, 60, 90, 80, "May-July", "December-February",
                "Rice thrives in wet conditions. Ensure adequate water supply during the growing period."
        ));

        // Cassava
        cropRecommendations.put("cassava", new CropWeatherRecommendation(
                "Cassava", 18, 35, 25, 50, 80, 60, "Year-round", "Peak dry season",
                "Cassava is drought-tolerant once established. Plant at the beginning of the rainy season."
        ));

        // Yam
        cropRecommendations.put("yam", new CropWeatherRecommendation(
                "Yam", 20, 32, 25, 60, 85, 70, "December-March", "July-September",
                "Yams require good soil moisture during early growth but are susceptible to waterlogging."
        ));

        // Plantain
        cropRecommendations.put("plantain", new CropWeatherRecommendation(
                "Plantain", 20, 35, 28, 60, 90, 75, "Year-round", "Peak dry season",
                "Plantains need consistent moisture. Mulch to retain soil moisture during dry periods."
        ));

        // Tomatoes
        cropRecommendations.put("tomatoes", new CropWeatherRecommendation(
                "Tomatoes", 18, 32, 24, 50, 75, 65, "October-March", "Peak rainy season",
                "Tomatoes are susceptible to fungal diseases in high humidity. Provide good air circulation."
        ));

        // Onions
        cropRecommendations.put("onions", new CropWeatherRecommendation(
                "Onions", 15, 30, 23, 40, 70, 60, "November-February", "Peak rainy season",
                "Onions require dry conditions during bulb formation and harvest. Avoid waterlogging."
        ));

        // Peppers
        cropRecommendations.put("peppers", new CropWeatherRecommendation(
                "Peppers", 18, 32, 25, 50, 80, 65, "October-March", "Peak rainy season",
                "Peppers need consistent moisture but are susceptible to fungal diseases in high humidity."
        ));

        // Cocoa
        cropRecommendations.put("cocoa", new CropWeatherRecommendation(
                "Cocoa", 20, 32, 25, 70, 95, 80, "April-October", "December-February",
                "Cocoa requires shade during establishment. Maintain humidity but avoid waterlogging."
        ));
    }

    public static Weather getCurrentWeather(String location) {
        try {
            String apiUrl = CURRENT_WEATHER_URL + "?q=" + location + "&units=metric&appid=" + API_KEY;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());

            // Parse weather data
            JSONArray weatherArray = jsonResponse.getJSONArray("weather");
            JSONObject weatherObject = weatherArray.getJSONObject(0);
            String description = weatherObject.getString("description");
            String icon = weatherObject.getString("icon");

            JSONObject mainObject = jsonResponse.getJSONObject("main");
            double temperature = mainObject.getDouble("temp");
            double feelsLike = mainObject.getDouble("feels_like");
            double humidity = mainObject.getDouble("humidity");

            JSONObject windObject = jsonResponse.getJSONObject("wind");
            double windSpeed = windObject.getDouble("speed");
            double windDirection = windObject.has("deg") ? windObject.getDouble("deg") : 0;

            long timestamp = jsonResponse.getLong("dt");
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

            // Create Weather object
            Weather weather = new Weather(
                    location,
                    description,
                    temperature,
                    feelsLike,
                    humidity,
                    windSpeed,
                    windDirection,
                    icon,
                    dateTime
            );

            // Add forecast data
            weather.setForecast(getForecast(location));

            return weather;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<WeatherForecast> getForecast(String location) {
        List<WeatherForecast> forecastList = new ArrayList<>();

        try {
            String apiUrl = FORECAST_URL + "?q=" + location + "&units=metric&appid=" + API_KEY;
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray forecastArray = jsonResponse.getJSONArray("list");

            for (int i = 0; i < forecastArray.length(); i++) {
                JSONObject forecastObject = forecastArray.getJSONObject(i);

                long timestamp = forecastObject.getLong("dt");
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());

                JSONArray weatherArray = forecastObject.getJSONArray("weather");
                JSONObject weatherObject = weatherArray.getJSONObject(0);
                String description = weatherObject.getString("description");
                String icon = weatherObject.getString("icon");

                JSONObject mainObject = forecastObject.getJSONObject("main");
                double temperature = mainObject.getDouble("temp");
                double minTemp = mainObject.getDouble("temp_min");
                double maxTemp = mainObject.getDouble("temp_max");
                double humidity = mainObject.getDouble("humidity");

                JSONObject windObject = forecastObject.getJSONObject("wind");
                double windSpeed = windObject.getDouble("speed");

                WeatherForecast forecast = new WeatherForecast(
                        dateTime,
                        description,
                        temperature,
                        minTemp,
                        maxTemp,
                        humidity,
                        windSpeed,
                        icon
                );

                forecastList.add(forecast);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return forecastList;
    }

    public static CropWeatherRecommendation getCropRecommendation(String cropName) {
        return cropRecommendations.getOrDefault(cropName.toLowerCase(), null);
    }

    public static boolean isFavorableWeather(String cropName, Weather weather) {
        CropWeatherRecommendation recommendation = getCropRecommendation(cropName.toLowerCase());
        if (recommendation == null || weather == null) {
            return false;
        }

        // Check if temperature is within range
        boolean tempInRange = weather.getTemperature() >= recommendation.getMinTemperature() &&
                weather.getTemperature() <= recommendation.getMaxTemperature();

        // Check if humidity is within range
        boolean humidityInRange = weather.getHumidity() >= recommendation.getMinHumidity() &&
                weather.getHumidity() <= recommendation.getMaxHumidity();

        // Basic check - both temp and humidity should be in acceptable ranges
        return tempInRange && humidityInRange;
    }

    public static String getWeatherAdvice(String cropName, Weather weather) {
        CropWeatherRecommendation recommendation = getCropRecommendation(cropName.toLowerCase());
        if (recommendation == null || weather == null) {
            return "No specific advice available. Please check general growing guidelines.";
        }

        StringBuilder advice = new StringBuilder();

        // Temperature advice
        if (weather.getTemperature() < recommendation.getMinTemperature()) {
            advice.append("• Current temperature is too low for optimal growth. ");
            advice.append("Consider protective measures or wait for warmer weather.\n");
        } else if (weather.getTemperature() > recommendation.getMaxTemperature()) {
            advice.append("• Current temperature is too high. ");
            advice.append("Provide shade and adequate water to prevent heat stress.\n");
        } else {
            advice.append("• Temperature conditions are favorable for growth.\n");
        }

        // Humidity advice
        if (weather.getHumidity() < recommendation.getMinHumidity()) {
            advice.append("• Current humidity is low. ");
            advice.append("Consider irrigation and mulching to maintain soil moisture.\n");
        } else if (weather.getHumidity() > recommendation.getMaxHumidity()) {
            advice.append("• Current humidity is high. ");
            advice.append("Ensure good air circulation to prevent fungal diseases.\n");
        } else {
            advice.append("• Humidity conditions are favorable.\n");
        }

        // Wind advice
        if (weather.getWindSpeed() > 10) {
            advice.append("• Current wind speeds are high. ");
            advice.append("Consider windbreaks to protect young plants.\n");
        }

        // General tips
        advice.append("\n").append(recommendation.getTips());

        return advice.toString();
    }
}