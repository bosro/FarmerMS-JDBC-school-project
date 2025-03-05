package com.example.jdbc_assignment.services;

import com.example.jdbc_assignment.models.Weather;
import com.example.jdbc_assignment.models.WeatherForecast;
import com.example.jdbc_assignment.models.CropWeatherRecommendation;
import com.example.jdbc_assignment.models.WeatherAlert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherService {
    private static final String API_KEY = "3c7559f67e4b724dfd19a499341ea1ba"; // Replace with your OpenWeatherMap API key
    //private static final String API_KEY = "08d2831fec0d3d2e9941f12c08497c43"; // New API key
    private static final String CURRENT_WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
//

    // Weather data cache to reduce API calls
    private static final Map<String, Weather> weatherCache = new HashMap<>();
    private static final long CACHE_EXPIRY_MINUTES = 30; // Cache expires after 30 minutes
    private static final Map<String, LocalDateTime> cacheTimestamps = new HashMap<>();

    // Cache for crop recommendations
    private static final Map<String, CropWeatherRecommendation> cropRecommendations = new HashMap<>();

    static {
        // Initialize crop recommendations specific to Ghana
        initializeGhanaianCropRecommendations();
    }

    private static void initializeGhanaianCropRecommendations() {
        // Maize (corn) - Major staple crop in Ghana
        cropRecommendations.put("maize", new CropWeatherRecommendation(
                "Maize (Corn)", 20, 35, 27, 50, 80, 65,
                "Major Season: March-April, Minor Season: August-September",
                "Major Season: July-August, Minor Season: November-December",
                "Plant at the onset of rains. In southern Ghana, plant in both major and minor rainy seasons. In northern Ghana, plant at the beginning of the single rainy season (May-June). Space plants 75cm between rows and 25cm within rows. Apply NPK fertilizer 2-3 weeks after planting. Common varieties include Obatanpa, Mamaba, and Dobidi."
        ));

        // Rice - Important in northern regions and irrigated areas
        cropRecommendations.put("rice", new CropWeatherRecommendation(
                "Rice", 20, 38, 30, 70, 90, 80,
                "Southern Ghana: April-May and September, Northern Ghana: May-July",
                "Southern Ghana: August and December, Northern Ghana: October-November",
                "In rainfed areas, plant at the onset of rains. For irrigated rice, planting can be done year-round if water is available. Use improved varieties like Jasmine 85, NERICA, and Togo Marshall. Transplant seedlings when they are 21 days old. In northern Ghana, rice is often grown in valley bottoms and lowlands."
        ));

        // Cassava - Major root crop
        cropRecommendations.put("cassava", new CropWeatherRecommendation(
                "Cassava", 20, 35, 28, 50, 80, 65,
                "Southern Ghana: March-April or September-October, Northern Ghana: May-June",
                "9-18 months after planting, depending on variety",
                "Use healthy stem cuttings 20-25cm long. Plant at the onset of rains for good establishment. In dry areas, plant at a 45° angle; in wet areas, plant vertically. Space plants 1m apart. Popular varieties include Afisiafi, Ankra, and Bankye hemaa. Cassava is drought-tolerant once established and can grow in poor soils."
        ));

        // Yam - Culturally important crop
        cropRecommendations.put("yam", new CropWeatherRecommendation(
                "Yam", 25, 35, 30, 60, 85, 70,
                "December-March (before rainy season)",
                "7-10 months after planting (August-December)",
                "Plant on mounds or ridges to improve drainage. Use seed yams or cut larger tubers (setts). The 'Pona' variety is highly valued in Ghanaian markets. Requires staking for better yields. Common in the middle belt and northern regions. Associated with important cultural practices like the Homowo and Yam festivals."
        ));

        // Plantain - Important in forest and transitional zones
        cropRecommendations.put("plantain", new CropWeatherRecommendation(
                "Plantain", 24, 35, 28, 60, 90, 80,
                "Southern Ghana: Beginning of rainy seasons (March-April or September)",
                "12-15 months after planting, then continuous harvest",
                "Use sword suckers from healthy mother plants. Dig planting holes 30×30×30cm and add organic matter. Space plants 3m apart. Mulch to retain soil moisture. Grows best in the forest and transitional zones where humidity is higher. Popular varieties include Apantu (False Horn) and Apem (French)."
        ));

        // Cocoa - Major export crop, grown in forest areas
        cropRecommendations.put("cocoa", new CropWeatherRecommendation(
                "Cocoa", 18, 32, 25, 70, 100, 80,
                "Main planting: April-June, Minor planting: September-October",
                "Main harvest: October-February, Light harvest: May-June",
                "Requires shade during establishment. Plant under temporary shade crops like plantain and banana. Grows best in Ghana's forest zone. Major varieties include Amazon, Amelonado, and hybrid types. Harvest pods when they turn yellow or orange. Break pods and ferment beans for 5-7 days before drying."
        ));

        // Groundnut (Peanut) - Important legume crop
        cropRecommendations.put("groundnut", new CropWeatherRecommendation(
                "Groundnut (Peanut)", 22, 33, 27, 50, 75, 65,
                "Major Season: April-May, Minor Season: September",
                "3-4 months after planting",
                "Plant at the beginning of the rainy season. Space plants 45cm between rows and 15cm within rows. Grows well in northern Ghana and the transitional zone. Improved varieties include Manipinta, Oboshie, and Obolo. Harvest when lower leaves turn yellow and inside pods show dark markings."
        ));

        // Tomatoes - Important vegetable
        cropRecommendations.put("tomatoes", new CropWeatherRecommendation(
                "Tomatoes", 20, 33, 26, 50, 70, 60,
                "Cool, dry season: October-March",
                "70-90 days after planting",
                "Tomatoes grow best during the dry season in Ghana due to lower disease pressure. Start seeds in nursery beds. Transplant when seedlings have 4-6 true leaves. Space plants 60cm apart. Stake plants to keep fruits off the ground. Major growing areas include Akumadan, Techiman, and parts of Upper East Region. Varieties include Pectomech, Tropimech, and Roma."
        ));

        // Onions - Common in northern regions
        cropRecommendations.put("onions", new CropWeatherRecommendation(
                "Onions", 15, 30, 24, 40, 70, 60,
                "Cool, dry season: November-February",
                "90-120 days after planting",
                "Grow best in the dry season. Widely cultivated in Upper East Region, especially the Bawku area. Start in nursery beds and transplant after 30-45 days. Space plants 10cm apart with 20cm between rows. Popular varieties include Bawku Red and Texas Grano. Stop watering when tops begin to fall over."
        ));

        // Pepper - Various types grown throughout Ghana
        cropRecommendations.put("pepper", new CropWeatherRecommendation(
                "Pepper", 20, 35, 27, 50, 80, 65,
                "Year-round with irrigation, or start of rainy seasons",
                "First harvest: 2-3 months after planting",
                "Many varieties grown in Ghana including 'Scotch Bonnet' (local name: Kpakpo shito), Legon 18, and Antomoshie. Start seeds in nursery beds. Transplant when seedlings have 4-6 true leaves. Space plants 60-75cm apart. Peppers can produce for several months with proper care."
        ));

        // Okra - Popular vegetable
        cropRecommendations.put("okra", new CropWeatherRecommendation(
                "Okra", 22, 35, 28, 60, 80, 70,
                "Start of rainy seasons: March-April or September",
                "45-60 days after planting",
                "Soak seeds overnight before planting to improve germination. Plant directly in the field, 2-3 seeds per hole. Space plants 60cm between rows and 30cm within rows. Common varieties include Asontem and Asutem. Harvest pods when young and tender (2-4 days after flowering) for best quality. Regular harvesting encourages more production."
        ));

        // Cowpea - Important legume
        cropRecommendations.put("cowpea", new CropWeatherRecommendation(
                "Cowpea (Beans)", 20, 35, 28, 40, 70, 60,
                "Northern Ghana: June-July, Southern Ghana: March-April or September",
                "60-90 days after planting",
                "Plant at the beginning of rains. Space 60cm between rows and 20cm within rows. Widely grown in northern Ghana. Varieties include Asontem, Hewale, and Asomdwee. Can be intercropped with maize or sorghum. Harvest when pods turn yellow but before they dry completely to prevent shattering."
        ));

        // Sweet Potato - Important root crop
        cropRecommendations.put("sweet potato", new CropWeatherRecommendation(
                "Sweet Potato", 20, 32, 24, 50, 80, 65,
                "Start of rainy seasons",
                "3-5 months after planting",
                "Use vine cuttings 30cm long with 3-4 nodes. Plant cuttings on ridges or mounds, 30cm apart. Grows well in all zones but particularly important in northern regions. Varieties include Apomuden (orange-fleshed, high vitamin A) and Faara. Requires minimal fertilizer. Harvest when leaves turn yellow."
        ));

        // Sorghum - Important cereal in northern Ghana
        cropRecommendations.put("sorghum", new CropWeatherRecommendation(
                "Sorghum", 20, 38, 27, 40, 70, 55,
                "Northern Ghana: June-July",
                "4-5 months after planting",
                "Plant at the onset of rains in northern Ghana. Space 75cm between rows and 30cm within rows. Drought-tolerant and grows well in areas with low rainfall. Important for food security in northern regions. Varieties include Kapaala and Dorado. Harvest when grains are hard and difficult to dent with fingernail."
        ));

        // Millet - Traditional cereal crop
        cropRecommendations.put("millet", new CropWeatherRecommendation(
                "Millet", 20, 38, 28, 30, 70, 50,
                "Northern Ghana: June-July",
                "3-4 months after planting",
                "Very drought-tolerant. Plant at the beginning of rains in northern Ghana. Space 50cm between rows and 20cm within rows. Important staple in northern communities. Pearl millet (Babala) is the most common type grown. Harvest when the grains are hard and difficult to split with fingernails."
        ));
    }

    public static Weather getCurrentWeather(String location) {
        // Check cache first
        if (weatherCache.containsKey(location)) {
            LocalDateTime cacheTime = cacheTimestamps.get(location);
            if (cacheTime != null &&
                    ChronoUnit.MINUTES.between(cacheTime, LocalDateTime.now()) < CACHE_EXPIRY_MINUTES) {
                return weatherCache.get(location);
            }
        }

        try {
            String apiUrl = CURRENT_WEATHER_URL + "?q=" + location + "&units=metric&appid=" + API_KEY;
            String response = makeApiRequest(apiUrl);

            if (response == null) return null;

            JSONObject jsonResponse = new JSONObject(response);

            // Check for error response
            if (jsonResponse.has("cod") && jsonResponse.getInt("cod") != 200) {
                System.err.println("API Error: " + jsonResponse.optString("message", "Unknown error"));
                return null;
            }

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

            // Get properly formatted location name
            String formattedLocation = jsonResponse.has("name") ?
                    jsonResponse.getString("name") + ", " +
                            jsonResponse.getJSONObject("sys").getString("country") :
                    location;

            // Create Weather object
            Weather weather = new Weather(
                    formattedLocation,
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
            List<WeatherForecast> forecast = getForecast(location);
            weather.setForecast(forecast);

            // Cache the weather data
            weatherCache.put(location, weather);
            cacheTimestamps.put(location, LocalDateTime.now());

            return weather;

        } catch (Exception e) {
            System.err.println("Error fetching weather data: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static List<WeatherForecast> getForecast(String location) {
        List<WeatherForecast> forecastList = new ArrayList<>();

        try {
            String apiUrl = FORECAST_URL + "?q=" + location + "&units=metric&appid=" + API_KEY;
            String response = makeApiRequest(apiUrl);

            if (response == null) return forecastList;

            JSONObject jsonResponse = new JSONObject(response);

            // Check for error response
            if (jsonResponse.has("cod") && !jsonResponse.getString("cod").equals("200")) {
                System.err.println("API Error: " + jsonResponse.optString("message", "Unknown error"));
                return forecastList;
            }

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
            System.err.println("Error fetching forecast data: " + e.getMessage());
            e.printStackTrace();
        }

        return forecastList;
    }

    private static String makeApiRequest(String apiUrl) {
        try {
            URL url = new URL(apiUrl.replace(" ", "%20"));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("API Request Failed: HTTP Error Code " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return response.toString();
        } catch (Exception e) {
            System.err.println("API Request Failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static CropWeatherRecommendation getCropRecommendation(String cropName) {
        return cropRecommendations.getOrDefault(cropName.toLowerCase(), null);
    }

    public static Map<String, CropWeatherRecommendation> getAllCropRecommendations() {
        return new HashMap<>(cropRecommendations);
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
            return "No specific advice available for this crop. Please check general growing guidelines.";
        }

        StringBuilder advice = new StringBuilder();

        // Temperature assessment - adjusting for Ghana's tropical climate
        if (weather.getTemperature() < recommendation.getMinTemperature()) {
            double diff = recommendation.getMinTemperature() - weather.getTemperature();
            if (diff > 5) {
                advice.append("• Current temperature is significantly below the optimal range (")
                        .append(String.format("%.1f°C vs. minimum %.1f°C", weather.getTemperature(), recommendation.getMinTemperature()))
                        .append("). Growth will be slowed. Consider waiting for warmer weather before planting.\n\n");
            } else {
                advice.append("• Current temperature is slightly below the optimal range (")
                        .append(String.format("%.1f°C vs. minimum %.1f°C", weather.getTemperature(), recommendation.getMinTemperature()))
                        .append("). Growth may be slightly slower than optimal.\n\n");
            }
        } else if (weather.getTemperature() > recommendation.getMaxTemperature()) {
            double diff = weather.getTemperature() - recommendation.getMaxTemperature();
            if (diff > 3) {
                advice.append("• Current temperature is significantly above the optimal range (")
                        .append(String.format("%.1f°C vs. maximum %.1f°C", weather.getTemperature(), recommendation.getMaxTemperature()))
                        .append("). This could cause heat stress. Provide shade and adequate water, especially during mid-day hours.\n\n");
            } else {
                advice.append("• Current temperature is slightly above the optimal range (")
                        .append(String.format("%.1f°C vs. maximum %.1f°C", weather.getTemperature(), recommendation.getMaxTemperature()))
                        .append("). Monitor plants for heat stress and provide additional water.\n\n");
            }
        } else {
            // Within optimal range
            double optDiff = Math.abs(weather.getTemperature() - recommendation.getOptimalTemperature());
            if (optDiff < 2) {
                advice.append("• Current temperature is ideal for ").append(cropName)
                        .append(" (").append(String.format("%.1f°C", weather.getTemperature())).append("). ")
                        .append("This provides excellent growing conditions.\n\n");
            } else {
                advice.append("• Current temperature is within the acceptable range for ").append(cropName)
                        .append(" (").append(String.format("%.1f°C", weather.getTemperature())).append("). ")
                        .append("Growing conditions are favorable.\n\n");
            }
        }

        // Humidity assessment - adjusting for Ghana's climate zones
        if (weather.getHumidity() < recommendation.getMinHumidity()) {
            double diff = recommendation.getMinHumidity() - weather.getHumidity();
            if (diff > 15) {
                advice.append("• Current humidity is significantly below optimal levels (")
                        .append(String.format("%.0f%% vs. minimum %.0f%%", weather.getHumidity(), recommendation.getMinHumidity()))
                        .append("). Plants may experience water stress. Consider irrigation, especially during dry season. Mulching can help retain soil moisture.\n\n");
            } else {
                advice.append("• Current humidity is slightly below optimal levels (")
                        .append(String.format("%.0f%% vs. minimum %.0f%%", weather.getHumidity(), recommendation.getMinHumidity()))
                        .append("). Consider additional irrigation during dry periods.\n\n");
            }
        } else if (weather.getHumidity() > recommendation.getMaxHumidity()) {
            double diff = weather.getHumidity() - recommendation.getMaxHumidity();
            if (diff > 15) {
                advice.append("• Current humidity is significantly above optimal levels (")
                        .append(String.format("%.0f%% vs. maximum %.0f%%", weather.getHumidity(), recommendation.getMaxHumidity()))
                        .append("). Risk of fungal diseases is high, especially during rainy season. Ensure proper spacing between plants and consider applying fungicides preventatively.\n\n");
            } else {
                advice.append("• Current humidity is slightly above optimal levels (")
                        .append(String.format("%.0f%% vs. maximum %.0f%%", weather.getHumidity(), recommendation.getMaxHumidity()))
                        .append("). Monitor for early signs of fungal diseases, which are common in Ghana's humid climate.\n\n");
            }
        } else {
            // Within optimal range
            double optDiff = Math.abs(weather.getHumidity() - recommendation.getOptimalHumidity());
            if (optDiff < 10) {
                advice.append("• Current humidity is ideal for ").append(cropName)
                        .append(" (").append(String.format("%.0f%%", weather.getHumidity())).append("). ")
                        .append("This provides excellent growing conditions.\n\n");
            } else {
                advice.append("• Current humidity is within the acceptable range for ").append(cropName)
                        .append(" (").append(String.format("%.0f%%", weather.getHumidity())).append("). ")
                        .append("Growing conditions are favorable.\n\n");
            }
        }

        // Wind assessment - adjusting for Harmattan and other Ghanaian conditions
        if (weather.getWindSpeed() > 8.0) {
            advice.append("• Current wind speed is high (")
                    .append(String.format("%.1f m/s", weather.getWindSpeed()))
                    .append("). During Harmattan season, this can increase evaporation and water stress. ")
                    .append("Consider windbreaks and ensure adequate irrigation.\n\n");
        } else if (weather.getWindSpeed() > 3.0) {
            advice.append("• Current wind conditions are moderate (")
                    .append(String.format("%.1f m/s", weather.getWindSpeed()))
                    .append("). This provides good air circulation but monitor soil moisture as wind can increase water loss.\n\n");
        } else {
            advice.append("• Current wind conditions are mild (")
                    .append(String.format("%.1f m/s", weather.getWindSpeed()))
                    .append("). In Ghana's humid climate, ensure adequate spacing between plants to promote air circulation and reduce disease risk.\n\n");
        }

        // Current season assessment - specific to Ghana's agricultural calendar
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();

        boolean isInNorth = isLocationInNorthernGhana(weather.getLocation());
        String seasonAdvice = getGhanaianSeasonalAdvice(cropName, currentMonth, isInNorth);
        advice.append(seasonAdvice);

        return advice.toString();
    }

    private static boolean isLocationInNorthernGhana(String location) {
        // Simple check for northern regions
        String locationLower = location.toLowerCase();
        return locationLower.contains("northern") ||
                locationLower.contains("upper east") ||
                locationLower.contains("upper west") ||
                locationLower.contains("tamale") ||
                locationLower.contains("bolgatanga") ||
                locationLower.contains("wa");
    }

    private static String getGhanaianSeasonalAdvice(String cropName, int currentMonth, boolean isInNorth) {
        if (isInNorth) {
            // Northern Ghana has one rainy season (May-October)
            if (currentMonth >= 5 && currentMonth <= 6) {
                return "• It is currently the start of the rainy season in northern Ghana. This is a good time for planting most crops including " + cropName + ".\n\n";
            } else if (currentMonth >= 7 && currentMonth <= 9) {
                return "• It is currently the peak rainy season in northern Ghana. Ensure proper drainage and monitor for waterlogging. Watch for fungal diseases which can spread rapidly in wet conditions.\n\n";
            } else if (currentMonth == 10) {
                return "• It is the end of the rainy season in northern Ghana. For " + cropName + ", this may be harvest time depending on when it was planted.\n\n";
            } else {
                return "• It is currently the dry season in northern Ghana. Irrigation will be necessary for growing " + cropName + ". Consider water conservation methods and drought-resistant varieties.\n\n";
            }
        } else {
            // Southern Ghana has two rainy seasons
            if (currentMonth >= 3 && currentMonth <= 6) {
                return "• It is currently the major rainy season in southern Ghana. This is a good time for planting " + cropName + ", but ensure proper drainage to prevent waterlogging.\n\n";
            } else if (currentMonth >= 9 && currentMonth <= 11) {
                return "• It is currently the minor rainy season in southern Ghana. This can be a good time for planting " + cropName + " depending on your specific location and variety.\n\n";
            } else if (currentMonth == 7 || currentMonth == 8) {
                return "• It is currently the minor dry season (July-August) in southern Ghana. Monitor soil moisture levels for " + cropName + " and provide irrigation if needed.\n\n";
            } else {
                return "• It is currently the major dry season in southern Ghana. For growing " + cropName + " during this period, irrigation will be necessary. The Harmattan winds (December-February) can increase water stress on plants.\n\n";
            }
        }
    }

    public static List<WeatherAlert> getWeatherAlerts(Weather weather) {
        List<WeatherAlert> alerts = new ArrayList<>();

        if (weather == null) return alerts;

        // Check for extreme temperature - adjusted for Ghana's climate
        if (weather.getTemperature() > 38) {
            LocalDateTime now = LocalDateTime.now();
            alerts.add(new WeatherAlert(
                    "Extreme Heat Warning",
                    "Unusually high temperatures may affect crops and livestock. Ensure adequate shade and water. Consider additional irrigation for crops, especially in the early morning or evening.",
                    "heat",
                    "severe",
                    now,
                    now.plusDays(1)
            ));
        } else if (weather.getTemperature() > 35) {
            LocalDateTime now = LocalDateTime.now();
            alerts.add(new WeatherAlert(
                    "Heat Advisory",
                    "High temperatures may cause mild heat stress to crops. Monitor soil moisture and provide additional water if needed. Consider shading for sensitive crops.",
                    "heat",
                    "moderate",
                    now,
                    now.plusDays(1)
            ));
        }

        // Check for high humidity (disease risk)
        if (weather.getHumidity() > 90) {
            LocalDateTime now = LocalDateTime.now();
            alerts.add(new WeatherAlert(
                    "High Humidity Alert",
                    "Extremely high humidity increases risk of fungal diseases in crops like tomatoes, peppers, and cocoa. Monitor for early signs of disease and ensure good air circulation between plants.",
                    "humidity",
                    "moderate",
                    now,
                    now.plusDays(1)
            ));
        }

        // Check for high winds - important during Harmattan
        if (weather.getWindSpeed() > 10) {
            LocalDateTime now = LocalDateTime.now();
            String alertType = "wind";
            String severity = "moderate";
            String title = "High Wind Advisory";
            String description = "Strong winds may damage crops, especially tall or recently planted varieties. Consider temporary windbreaks or support structures.";

            // Check if it's likely Harmattan season (December-February)
            if (now.getMonthValue() >= 12 || now.getMonthValue() <= 2) {
                title = "Harmattan Wind Advisory";
                description = "Dry Harmattan winds increase evaporation and water stress on plants. Increase irrigation frequency, apply mulch to retain soil moisture, and protect seedlings.";
                severity = "moderate";
            }

            alerts.add(new WeatherAlert(
                    title,
                    description,
                    alertType,
                    severity,
                    now,
                    now.plusDays(1)
            ));
        }

        // Check forecasts for upcoming rain - critical for farming decisions
        List<WeatherForecast> forecasts = weather.getForecast();
        if (forecasts != null && !forecasts.isEmpty()) {
            boolean heavyRainWarning = false;
            boolean rainAlert = false;

            for (int i = 0; i < Math.min(8, forecasts.size()); i++) {
                WeatherForecast forecast = forecasts.get(i);
                String desc = forecast.getDescription().toLowerCase();

                if (desc.contains("heavy rain") || desc.contains("thunderstorm")) {
                    heavyRainWarning = true;
                    break;
                } else if (desc.contains("rain") || desc.contains("shower")) {
                    rainAlert = true;
                }
            }

            if (heavyRainWarning) {
                LocalDateTime now = LocalDateTime.now();
                alerts.add(new WeatherAlert(
                        "Heavy Rain Warning",
                        "Heavy rainfall expected in the next 24 hours. In Ghana's terrain, this can lead to flash flooding and erosion. Ensure proper drainage in fields to prevent waterlogging. Consider delaying planting or harvesting operations.",
                        "rain",
                        "severe",
                        now,
                        now.plusDays(1)
                ));
            } else if (rainAlert) {
                LocalDateTime now = LocalDateTime.now();
                alerts.add(new WeatherAlert(
                        "Rain Advisory",
                        "Rain expected in the next 24 hours. Consider the timing of field operations, fertilizer or pesticide applications that require dry conditions. For newly planted crops, this rain will be beneficial for establishment.",
                        "rain",
                        "minor",
                        now,
                        now.plusDays(1)
                ));
            }
        }

        // Add seasonal alerts based on Ghana's agricultural calendar
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();

        // Check for Harmattan season
        if (currentMonth == 12 || currentMonth == 1 || currentMonth == 2) {
            alerts.add(new WeatherAlert(
                    "Harmattan Season",
                    "The Harmattan season brings dry, dusty conditions across Ghana. Crops may experience increased water stress. Increase irrigation frequency and consider dust protection for sensitive crops like vegetables.",
                    "season",
                    "moderate",
                    now,
                    now.plusDays(30)
            ));
        }

        // Start of rainy season alerts - different for northern and southern Ghana
        boolean isInNorth = isLocationInNorthernGhana(weather.getLocation());

        if (isInNorth && currentMonth == 5) {
            alerts.add(new WeatherAlert(
                    "Rainy Season Beginning",
                    "The rainy season is beginning in northern Ghana. Prepare fields for planting staple crops like maize, sorghum, and millet. Early planting can maximize the growing season.",
                    "season",
                    "minor",
                    now,
                    now.plusDays(30)
            ));
        } else if (!isInNorth && (currentMonth == 3 || currentMonth == 9)) {
            String seasonType = (currentMonth == 3) ? "major" : "minor";
            alerts.add(new WeatherAlert(
                    seasonType + " Rainy Season Beginning",
                    "The " + seasonType + " rainy season is beginning in southern Ghana. Prepare fields for planting. This is a good time for planting crops like maize, cassava, and vegetables.",
                    "season",
                    "minor",
                    now,
                    now.plusDays(30)
            ));
        }

        return alerts;
    }
}