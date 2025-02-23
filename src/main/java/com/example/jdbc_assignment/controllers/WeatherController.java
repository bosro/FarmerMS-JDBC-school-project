package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.Main;
import com.example.jdbc_assignment.models.CropWeatherRecommendation;
import com.example.jdbc_assignment.models.Weather;
import com.example.jdbc_assignment.models.WeatherForecast;
import com.example.jdbc_assignment.services.WeatherService;
import com.example.jdbc_assignment.utils.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.geometry.Pos;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class WeatherController {

    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private Button weatherButton;

    @FXML private TextField locationField;
    @FXML private FontIcon weatherIcon;
    @FXML private Text temperatureText;
    @FXML private Text weatherDescription;
    @FXML private Text locationText;
    @FXML private Text dateTimeText;
    @FXML private Text feelsLikeText;
    @FXML private Text humidityText;
    @FXML private Text windText;
    @FXML private Text windDirectionText;
    @FXML private Text userNameText;

    @FXML private ComboBox<String> produceComboBox;
    @FXML private VBox adviceContainer;
    @FXML private Text cropNameText;
    @FXML private Text tempRangeText;
    @FXML private VBox currentWeatherCard;

    private Weather currentWeather;

    @FXML
    public void initialize() {
        // Set user name
        if (SessionManager.getCurrentUser() != null) {
            userNameText.setText(SessionManager.getCurrentUser().getFullName());
        }

        // Fill produce combobox with available crops
        ObservableList<String> cropList = FXCollections.observableArrayList(
                "Maize", "Rice", "Cassava", "Yam", "Plantain",
                "Tomatoes", "Onions", "Peppers", "Cocoa"
        );
        produceComboBox.setItems(cropList);

        // Default location for Ghana farmers
        locationField.setText("Accra, Ghana");
    }

    @FXML
    private void handleGetWeatherAction() {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            // Show error message
            return;
        }

        // Show loading indicator or message
        temperatureText.setText("Loading...");
        weatherDescription.setText("");

        // Fetch weather data in background thread
        new Thread(() -> {
            Weather weather = WeatherService.getCurrentWeather(location);

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                if (weather != null) {
                    updateWeatherUI(weather);
                    currentWeather = weather;
                } else {
                    // Show error message
                    temperatureText.setText("--°C");
                    weatherDescription.setText("Error fetching weather data");
                }
            });
        }).start();
    }

    private void updateForecastUI(List<WeatherForecast> forecasts) {
        HBox forecastContainer = (HBox) currentWeatherCard.getScene().lookup("#forecastContainer");
        if (forecastContainer != null) {
            forecastContainer.getChildren().clear();

            // Group forecasts by day (taking first forecast of each day)
            Map<LocalDate, WeatherForecast> dailyForecasts = new HashMap<>();
            for (WeatherForecast forecast : forecasts) {
                LocalDate forecastDate = forecast.getDate().toLocalDate();
                if (!dailyForecasts.containsKey(forecastDate)) {
                    dailyForecasts.put(forecastDate, forecast);
                }
            }

            // Create forecast day cards
            for (Map.Entry<LocalDate, WeatherForecast> entry : dailyForecasts.entrySet().stream()
                    .limit(5) // Limit to 5 days
                    .collect(Collectors.toList())) {

                LocalDate date = entry.getKey();
                WeatherForecast forecast = entry.getValue();

                VBox dayCard = new VBox();
                dayCard.getStyleClass().add("forecast-day");
                dayCard.setAlignment(Pos.CENTER);
                dayCard.setSpacing(5);

                Text dateText = new Text(date.format(DateTimeFormatter.ofPattern("E, MMM d")));
                dateText.getStyleClass().add("forecast-date");

                FontIcon icon = new FontIcon();
                icon.setIconSize(32);
                icon.getStyleClass().add("forecast-icon");

                // Set icon based on weather condition
                String iconCode = forecast.getIcon();
                if (iconCode != null && !iconCode.isEmpty()) {
                    // Use same icon mapping logic as in updateWeatherUI
                    switch (iconCode) {
                        case "01d": case "01n": icon.setIconLiteral("fas-sun"); break;
                        case "02d": case "02n": icon.setIconLiteral("fas-cloud-sun"); break;
                        case "03d": case "03n": case "04d": case "04n": icon.setIconLiteral("fas-cloud"); break;
                        case "09d": case "09n": icon.setIconLiteral("fas-cloud-showers-heavy"); break;
                        case "10d": case "10n": icon.setIconLiteral("fas-cloud-rain"); break;
                        case "11d": case "11n": icon.setIconLiteral("fas-bolt"); break;
                        case "13d": case "13n": icon.setIconLiteral("fas-snowflake"); break;
                        case "50d": case "50n": icon.setIconLiteral("fas-smog"); break;
                        default: icon.setIconLiteral("fas-cloud"); break;
                    }
                } else {
                    icon.setIconLiteral("fas-cloud");
                }

                Text tempText = new Text(String.format("%.1f°C", forecast.getTemperature()));
                tempText.getStyleClass().add("forecast-temp");

                Text descText = new Text(forecast.getDescription());
                descText.getStyleClass().add("forecast-desc");

                dayCard.getChildren().addAll(dateText, icon, tempText, descText);
                forecastContainer.getChildren().add(dayCard);
            }
        }
    }

    private void updateWeatherUI(Weather weather) {
        // Update the main weather information
        temperatureText.setText(String.format("%.1f°C", weather.getTemperature()));
        weatherDescription.setText(weather.getDescription());
        locationText.setText(weather.getLocation());
        dateTimeText.setText(weather.getTimestamp().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")));
        feelsLikeText.setText(String.format("%.1f°C", weather.getFeelsLike()));
        humidityText.setText(String.format("%.0f%%", weather.getHumidity()));
        windText.setText(String.format("%.1f m/s", weather.getWindSpeed()));
        windDirectionText.setText(String.format("%.0f°", weather.getWindDirection()));

        // Set appropriate weather icon
        String iconCode = weather.getIcon();
        if (iconCode != null && !iconCode.isEmpty()) {
            // Map OpenWeatherMap icon codes to FontAwesome icons
            switch (iconCode) {
                case "01d": // clear sky (day)
                    weatherIcon.setIconLiteral("fas-sun");
                    break;
                case "01n": // clear sky (night)
                    weatherIcon.setIconLiteral("fas-moon");
                    break;
                case "02d": // few clouds (day)
                case "02n": // few clouds (night)
                    weatherIcon.setIconLiteral("fas-cloud-sun");
                    break;
                case "03d": // scattered clouds (day)
                case "03n": // scattered clouds (night)
                case "04d": // broken clouds (day)
                case "04n": // broken clouds (night)
                    weatherIcon.setIconLiteral("fas-cloud");
                    break;
                case "09d": // shower rain (day)
                case "09n": // shower rain (night)
                    weatherIcon.setIconLiteral("fas-cloud-showers-heavy");
                    break;
                case "10d": // rain (day)
                case "10n": // rain (night)
                    weatherIcon.setIconLiteral("fas-cloud-rain");
                    break;
                case "11d": // thunderstorm (day)
                case "11n": // thunderstorm (night)
                    weatherIcon.setIconLiteral("fas-bolt");
                    break;
                case "13d": // snow (day)
                case "13n": // snow (night)
                    weatherIcon.setIconLiteral("fas-snowflake");
                    break;
                case "50d": // mist (day)
                case "50n": // mist (night)
                    weatherIcon.setIconLiteral("fas-smog");
                    break;
                default:
                    weatherIcon.setIconLiteral("fas-cloud");
                    break;
            }
        }

        if (weather.getForecast() != null && !weather.getForecast().isEmpty()) {
            updateForecastUI(weather.getForecast());
        }

        // Display the weather card
        currentWeatherCard.setVisible(true);
    }

    @FXML
    private void handleGetAdviceAction() {
        String selectedCrop = produceComboBox.getValue();
        if (selectedCrop == null || currentWeather == null) {
            return;
        }

        // Get crop recommendation
        CropWeatherRecommendation recommendation =
                WeatherService.getCropRecommendation(selectedCrop.toLowerCase());

        if (recommendation != null) {
            // Update UI with recommendation data
            cropNameText.setText(recommendation.getCrop());
            tempRangeText.setText(
                    String.format("%.1f to %.1f°C",
                            recommendation.getMinTemperature(),
                            recommendation.getMaxTemperature())
            );

            // Create the advice text
            String advice = WeatherService.getWeatherAdvice(selectedCrop, currentWeather);

            // Find or create the advice text element and update it
            Text adviceText = new Text(advice);
            adviceText.getStyleClass().add("advice-text");

            // Clear previous advice if any
            adviceContainer.getChildren().removeIf(node -> node instanceof Text &&
                    ((Text) node).getText().contains("•"));

            // Add the new advice
            adviceContainer.getChildren().add(adviceText);

            // Show the advice container
            adviceContainer.setVisible(true);
        }
    }

    // Navigation handlers
    @FXML
    private void handleDashboardAction() {
        navigateTo("dashboard.fxml");
    }

    @FXML
    private void handleProduceAction() {
        navigateTo("produce.fxml");
    }

    @FXML
    private void handleInventoryAction() {
        navigateTo("inventory.fxml");
    }

    @FXML
    private void handleSalesAction() {
        navigateTo("sales.fxml");
    }

    @FXML
    private void handleMarketPricesAction() {
        navigateTo("market-prices.fxml");
    }

    @FXML
    private void handleWeatherAction() {
        // Already on weather screen, no need to navigate
    }

    @FXML
    private void handleProfileAction() {
        navigateTo("profile.fxml");
    }

    @FXML
    private void handleLogoutAction() {
        SessionManager.clearSession();
        navigateTo("login.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource("views/" + fxml));
            Scene scene = weatherButton.getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}