package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.dao.ProduceDAO;
import com.example.jdbc_assignment.models.CropWeatherRecommendation;
import com.example.jdbc_assignment.models.Produce;
import com.example.jdbc_assignment.models.Weather;
import com.example.jdbc_assignment.models.WeatherForecast;
import com.example.jdbc_assignment.models.WeatherAlert;
import com.example.jdbc_assignment.services.WeatherService;
import com.example.jdbc_assignment.utils.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;


import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.geometry.Side;

public class WeatherController extends BaseController implements Initializable {

    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private Button weatherButton;
    @FXML private Text userNameText;

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
    @FXML private HBox forecastContainer;
    @FXML private TabPane weatherTabPane;
    @FXML private VBox currentWeatherCard;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private ComboBox<String> produceComboBox;
    @FXML private VBox adviceContainer;
    @FXML private Text cropNameText;
    @FXML private Text tempRangeText;

    @FXML private VBox alertsContainer;

    @FXML private LineChart<String, Number> tempTrendChart;
    @FXML private LineChart<String, Number> rainTrendChart;

    @FXML private VBox calendarContainer;
    @FXML private ComboBox<String> monthSelector;
    @FXML private ComboBox<String> regionSelector;

    private Weather currentWeather;
    private ProduceDAO produceDAO = new ProduceDAO();
    private Map<String, CropWeatherRecommendation> cropRecommendations = new HashMap<>();

    // Common Ghanaian regions and cities for location suggestions
    private final List<String> GHANA_LOCATIONS = Arrays.asList(
            "Accra, Ghana", "Kumasi, Ghana", "Tamale, Ghana", "Takoradi, Ghana",
            "Cape Coast, Ghana", "Sunyani, Ghana", "Bolgatanga, Ghana", "Wa, Ghana",
            "Ho, Ghana", "Koforidua, Ghana", "Techiman, Ghana", "Obuasi, Ghana"
    );

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set user name
        if (SessionManager.getCurrentUser() != null) {
            userNameText.setText(SessionManager.getCurrentUser().getFullName());
        }

        // Mark weather button as active
        resetNavButtonStyles();
        weatherButton.getStyleClass().add("active-nav-item");

        // Set up loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
        }

        // Initialize components
        initializeProduceComboBox();
        initializeMonthSelector();
        initializeRegionSelector();

        // Default location for Ghana farmers - can be set to different default regions based on user profile
        locationField.setText("Accra, Ghana");

        // Load weather data for default location
        handleGetWeatherAction();
    }

    private void initializeProduceComboBox() {
        // First get the farmer's own crops
        ObservableList<String> cropList = FXCollections.observableArrayList();

        // Add the farmer's own produce if available
        if (SessionManager.getCurrentUserId() > 0) {
            List<Produce> farmerProduce = produceDAO.getAllProduceByUser(SessionManager.getCurrentUserId());
            for (Produce produce : farmerProduce) {
                cropList.add(produce.getName());
            }
        }

        // Add common Ghanaian crops if the list is empty or has few items
        if (cropList.size() < 5) {
            cropList.addAll(
                    "Maize", "Rice", "Cassava", "Yam", "Plantain",
                    "Cocoa", "Groundnut", "Tomatoes", "Onions", "Pepper",
                    "Okra", "Cowpea", "Sweet Potato", "Sorghum", "Millet"
            );

            // Remove duplicates
            cropList = cropList.stream().distinct().collect(
                    Collectors.toCollection(FXCollections::observableArrayList)
            );
        }

        produceComboBox.setItems(cropList);

        // Select first crop by default
        if (!cropList.isEmpty()) {
            produceComboBox.setValue(cropList.get(0));
        }
    }

    private void initializeMonthSelector() {
        ObservableList<String> months = FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        );
        monthSelector.setItems(months);

        // Set current month
        int currentMonth = LocalDate.now().getMonthValue() - 1;
        monthSelector.setValue(months.get(currentMonth));

        // Add listener for month changes
        monthSelector.setOnAction(e -> updatePlantingCalendar());
    }

    private void initializeRegionSelector() {
        if (regionSelector != null) {
            ObservableList<String> regions = FXCollections.observableArrayList(
                    "All Regions",
                    "Greater Accra",
                    "Ashanti",
                    "Northern",
                    "Western",
                    "Eastern",
                    "Central",
                    "Volta",
                    "Brong-Ahafo",
                    "Upper East",
                    "Upper West"
            );
            regionSelector.setItems(regions);
            regionSelector.setValue("All Regions");

            // Add listener for region changes
            regionSelector.setOnAction(e -> updatePlantingCalendar());
        }
    }

    @FXML
    private void handleGetWeatherAction() {
        String location = locationField.getText().trim();
        if (location.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter a location");
            return;
        }

        // Add Ghana as suffix if not specified
        if (!location.toLowerCase().contains("ghana")) {
            location += ", Ghana";
            locationField.setText(location);
        }

        // Show loading indicator
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(true);
        }

        // Clear existing weather data display
        temperatureText.setText("--°C");
        weatherDescription.setText("Loading...");

        // Create a background task for fetching weather
        String finalLocation = location;
        Task<Weather> weatherTask = new Task<Weather>() {
            @Override
            protected Weather call() throws Exception {
                return WeatherService.getCurrentWeather(finalLocation);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    Weather weather = getValue();
                    if (weather != null) {
                        currentWeather = weather;
                        updateWeatherUI(weather);
                        updateWeatherTrends(weather);
                        updateWeatherAlerts(weather);
                        updatePlantingCalendar();

                        // If a crop is selected, update advice
                        if (produceComboBox.getValue() != null) {
                            handleGetAdviceAction();
                        }
                    } else {
                        temperatureText.setText("--°C");
                        weatherDescription.setText("Error fetching weather data");
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Could not fetch weather data for this location. Please check your Internet connection and try again.");
                    }

                    // Hide loading indicator
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    temperatureText.setText("--°C");
                    weatherDescription.setText("Error fetching weather data");
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Could not fetch weather data: " + getException().getMessage());

                    // Hide loading indicator
                    if (loadingIndicator != null) {
                        loadingIndicator.setVisible(false);
                    }
                });
            }
        };

        // Start the background task
        new Thread(weatherTask).start();
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
        windDirectionText.setText(getWindDirection(weather.getWindDirection()));

        // Set appropriate weather icon
        setWeatherIcon(weather.getIcon());

        // Update forecast
        if (weather.getForecast() != null && !weather.getForecast().isEmpty()) {
            updateForecastUI(weather.getForecast());
        }

        // Display the weather card
        currentWeatherCard.setVisible(true);
    }

    private String getWindDirection(double degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        return directions[(int)Math.round(degrees % 360 / 45) % 8] +
                String.format(" (%.0f°)", degrees);
    }

    private void setWeatherIcon(String iconCode) {
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
                    weatherIcon.setIconLiteral("fas-cloud-sun");
                    break;
                case "02n": // few clouds (night)
                    weatherIcon.setIconLiteral("fas-cloud-moon");
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
        } else {
            weatherIcon.setIconLiteral("fas-cloud");
        }
    }

    private void updateForecastUI(List<WeatherForecast> forecasts) {
        if (forecastContainer == null) {
            // If the forecastContainer is null, try to find it
            forecastContainer = (HBox) currentWeatherCard.getScene().lookup("#forecastContainer");

            // If still null, create it
            if (forecastContainer == null) {
                return;
            }
        }

        forecastContainer.getChildren().clear();
        forecastContainer.setSpacing(10);
        forecastContainer.setPadding(new Insets(10));

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
            dayCard.setPrefWidth(120);
            dayCard.setPadding(new Insets(10));


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

            Text humidityText = new Text(String.format("%.0f%% Humidity", forecast.getHumidity()));
            humidityText.getStyleClass().add("forecast-humidity");

            Text windText = new Text(String.format("%.1f m/s", forecast.getWindSpeed()));
            windText.getStyleClass().add("forecast-wind");

            dayCard.getChildren().addAll(dateText, icon, tempText, descText, humidityText, windText);
            forecastContainer.getChildren().add(dayCard);
        }
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
            // Clear previous advice
            adviceContainer.getChildren().clear();

            // Add crop icon and name header
            HBox cropHeader = new HBox(10);
            cropHeader.setAlignment(Pos.CENTER_LEFT);

            FontIcon cropIcon = new FontIcon("fas-seedling");
            cropIcon.setIconSize(24);
            cropIcon.setIconColor(javafx.scene.paint.Color.GREEN);

            Text cropTitle = new Text(recommendation.getCrop());
            cropTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

            cropHeader.getChildren().addAll(cropIcon, cropTitle);
            adviceContainer.getChildren().add(cropHeader);

            // Add temperature range
            HBox tempRange = new HBox(10);
            tempRange.setAlignment(Pos.CENTER_LEFT);

            FontIcon tempIcon = new FontIcon("fas-thermometer-half");
            tempIcon.setIconSize(16);

            Text tempText = new Text(String.format("Optimal Temperature: %.1f to %.1f°C",
                    recommendation.getMinTemperature(), recommendation.getMaxTemperature()));

            tempRange.getChildren().addAll(tempIcon, tempText);
            adviceContainer.getChildren().add(tempRange);

            // Add humidity range
            HBox humidityRange = new HBox(10);
            humidityRange.setAlignment(Pos.CENTER_LEFT);

            FontIcon humidityIcon = new FontIcon("fas-tint");
            humidityIcon.setIconSize(16);

            Text humidityText = new Text(String.format("Optimal Humidity: %.0f to %.0f%%",
                    recommendation.getMinHumidity(), recommendation.getMaxHumidity()));

            humidityRange.getChildren().addAll(humidityIcon, humidityText);
            adviceContainer.getChildren().add(humidityRange);

            // Add planting season
            HBox plantingRange = new HBox(10);
            plantingRange.setAlignment(Pos.CENTER_LEFT);

            FontIcon calendarIcon = new FontIcon("fas-calendar-alt");
            calendarIcon.setIconSize(16);

            Text plantingText = new Text("Planting Season: " + recommendation.getPlantingSeason());

            plantingRange.getChildren().addAll(calendarIcon, plantingText);
            adviceContainer.getChildren().add(plantingRange);

            // Add harvest season
            HBox harvestRange = new HBox(10);
            harvestRange.setAlignment(Pos.CENTER_LEFT);

            FontIcon harvestIcon = new FontIcon("fas-calendar-check");
            harvestIcon.setIconSize(16);

            Text harvestText = new Text("Harvest Season: " + recommendation.getHarvestSeason());

            harvestRange.getChildren().addAll(harvestIcon, harvestText);
            adviceContainer.getChildren().add(harvestRange);

            // Add advice separator
            Region separator = new Region();
            separator.setPrefHeight(20);
            adviceContainer.getChildren().add(separator);

            // Add current advice based on weather conditions
            Text adviceHeader = new Text("Current Weather Assessment");
            adviceHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            adviceContainer.getChildren().add(adviceHeader);

            // Get the advice text
            String advice = WeatherService.getWeatherAdvice(selectedCrop, currentWeather);

            // Split advice by paragraph and add each with bullet points
            String[] paragraphs = advice.split("\n\n");
            for (String paragraph : paragraphs) {
                if (!paragraph.trim().isEmpty()) {
                    Text adviceText = new Text(paragraph);
                    adviceText.setWrappingWidth(400);
                    adviceContainer.getChildren().add(adviceText);

                    // Add a small spacer
                    Region paraSpace = new Region();
                    paraSpace.setPrefHeight(10);
                    adviceContainer.getChildren().add(paraSpace);
                }
            }

            // Add tips
            Region tipSeparator = new Region();
            tipSeparator.setPrefHeight(20);
            adviceContainer.getChildren().add(tipSeparator);

            Text tipsHeader = new Text("Growing Tips for " + recommendation.getCrop());
            tipsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            adviceContainer.getChildren().add(tipsHeader);

            // Split tips by sentence and add them with bullet points
            String[] tips = recommendation.getTips().split("\\.");
            for (String tip : tips) {
                tip = tip.trim();
                if (!tip.isEmpty()) {
                    HBox tipBox = new HBox(10);
                    tipBox.setAlignment(Pos.CENTER_LEFT);

                    FontIcon bulletIcon = new FontIcon("fas-check-circle");
                    bulletIcon.setIconSize(12);
                    bulletIcon.setIconColor(javafx.scene.paint.Color.LIGHTGREEN);

                    Text tipText = new Text(tip + ".");
                    tipText.setWrappingWidth(380);

                    tipBox.getChildren().addAll(bulletIcon, tipText);
                    adviceContainer.getChildren().add(tipBox);
                }
            }
        }
    }

    private void updateWeatherAlerts(Weather weather) {
        if (alertsContainer == null) return;

        alertsContainer.getChildren().clear();

        // Get alerts from service
        List<WeatherAlert> alerts = WeatherService.getWeatherAlerts(weather);

        if (alerts.isEmpty()) {
            // Instead of just showing "no alerts", provide some general information
            VBox infoBox = new VBox(10);
            infoBox.setStyle("-fx-background-color: #2A2A2A; -fx-background-radius: 8px; -fx-padding: 15px;");

            Text noAlertsTitle = new Text("No Weather Alerts for This Area");
            noAlertsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            Text seasonInfo = new Text(getSeasonalInfoForGhana());
            seasonInfo.setWrappingWidth(400);

            infoBox.getChildren().addAll(noAlertsTitle, seasonInfo);
            alertsContainer.getChildren().add(infoBox);
            return;
        }

        // Add each alert
        for (WeatherAlert alert : alerts) {
            VBox alertBox = new VBox(5);
            alertBox.setStyle("-fx-background-color: " + getAlertColor(alert.getSeverity()) + "; " +
                    "-fx-background-radius: 5px; -fx-padding: 10px; -fx-margin: 5px;");

            HBox headerBox = new HBox(10);
            headerBox.setAlignment(Pos.CENTER_LEFT);

            FontIcon alertIcon = new FontIcon(getAlertIcon(alert.getType()));
            alertIcon.setIconSize(24);

            Text titleText = new Text(alert.getTitle());
            titleText.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

            headerBox.getChildren().addAll(alertIcon, titleText);

            Text descriptionText = new Text(alert.getDescription());
            descriptionText.setWrappingWidth(400);

            Text periodText = new Text("Effective: " +
                    alert.getStartTime().format(DateTimeFormatter.ofPattern("MMM d, HH:mm")) + " to " +
                    alert.getEndTime().format(DateTimeFormatter.ofPattern("MMM d, HH:mm")));
            periodText.setStyle("-fx-font-style: italic;");

            alertBox.getChildren().addAll(headerBox, descriptionText, periodText);
            alertsContainer.getChildren().add(alertBox);
        }
    }

    private String getSeasonalInfoForGhana() {
        LocalDateTime now = LocalDateTime.now();
        int month = now.getMonthValue();

        if (month == 12 || month <= 2) {
            return "Current Season: Major Dry Season (Harmattan)\n\n" +
                    "This period is characterized by hot, dry days and cooler nights, with dusty winds from the Sahara (Harmattan). " +
                    "It's typically planting season for irrigated vegetables and land preparation time for rainy season crops. " +
                    "Irrigation is necessary for most crops during this period. The Harmattan effect is stronger in northern Ghana.";
        } else if (month >= 3 && month <= 6) {
            return "Current Season: Major Rainy Season (Southern Ghana)\n\n" +
                    "This is the main growing season in southern Ghana, with regular rainfall allowing for planting of major staple crops. " +
                    "In northern Ghana, the rainy season usually starts later (May-June). " +
                    "It's a good time to plant maize, yam, cassava, and most other staple crops in the south.";
        } else if (month == 7 || month == 8) {
            return "Current Season: Minor Dry Season (Southern Ghana)\n\n" +
                    "This is a short dry period in southern Ghana, though rainfall doesn't completely stop. " +
                    "In northern Ghana, this is the peak of the single rainy season. " +
                    "Crops planted during the major rainy season in the south may be maturing now. " +
                    "It's a good time for harvesting certain crops in the south and continued planting in the north.";
        } else { // month 9-11
            return "Current Season: Minor Rainy Season (Southern Ghana)\n\n" +
                    "This is the second rainy season in southern Ghana, providing another opportunity for planting short-cycle crops. " +
                    "In northern Ghana, this is the end of the single rainy season and early harvesting period. " +
                    "It's a good time to plant vegetables and second-season maize in southern Ghana, while northern farmers may be preparing for harvest.";
        }
    }

    private String getAlertColor(String severity) {
        switch (severity.toLowerCase()) {
            case "severe": return "rgba(244, 67, 54, 0.7)";
            case "moderate": return "rgba(255, 152, 0, 0.7)";
            case "minor": return "rgba(255, 235, 59, 0.7)";
            default: return "rgba(33, 150, 243, 0.7)";
        }
    }

    private String getAlertIcon(String type) {
        switch (type.toLowerCase()) {
            case "storm": return "fas-bolt";
            case "rain": return "fas-cloud-showers-heavy";
            case "heat": return "fas-temperature-high";
            case "cold": return "fas-temperature-low";
            case "wind": return "fas-wind";
            case "humidity": return "fas-tint";
            case "season": return "fas-calendar-alt";
            default: return "fas-exclamation-triangle";
        }
    }

    private void updateWeatherTrends(Weather weather) {
        if (tempTrendChart == null || rainTrendChart == null) return;

        // Clear previous data
        tempTrendChart.getData().clear();
        rainTrendChart.getData().clear();

        // Get weather forecast data
        List<WeatherForecast> forecasts = weather.getForecast();
        if (forecasts == null || forecasts.isEmpty()) return;

        // Create temperature series
        XYChart.Series<String, Number> tempSeries = new XYChart.Series<>();
        tempSeries.setName("Temperature (°C)");

        XYChart.Series<String, Number> maxTempSeries = new XYChart.Series<>();
        maxTempSeries.setName("Max Temperature (°C)");

        XYChart.Series<String, Number> minTempSeries = new XYChart.Series<>();
        minTempSeries.setName("Min Temperature (°C)");

        // Create humidity/rain probability series
        XYChart.Series<String, Number> humiditySeries = new XYChart.Series<>();
        humiditySeries.setName("Humidity (%)");

        // Add data points - using fewer points for better readability
        for (int i = 0; i < forecasts.size(); i += 2) {
            WeatherForecast forecast = forecasts.get(i);
            String label = forecast.getDate().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));

            tempSeries.getData().add(new XYChart.Data<>(label, forecast.getTemperature()));
            maxTempSeries.getData().add(new XYChart.Data<>(label, forecast.getMaxTemperature()));
            minTempSeries.getData().add(new XYChart.Data<>(label, forecast.getMinTemperature()));
            humiditySeries.getData().add(new XYChart.Data<>(label, forecast.getHumidity()));
        }

        // Add series to charts
        tempTrendChart.getData().addAll(tempSeries, maxTempSeries, minTempSeries);
        rainTrendChart.getData().add(humiditySeries);

        // Style the charts
        tempTrendChart.setCreateSymbols(false);
        rainTrendChart.setCreateSymbols(false);

        // Add farming guidance text based on forecast trends
        if (forecasts.size() > 0) {
            addForecastGuidance(forecasts);
        }
    }

    private void addForecastGuidance(List<WeatherForecast> forecasts) {
        // Skip if we don't have charts
        if (tempTrendChart == null || tempTrendChart.getParent() == null) return;

        // Calculate average values from forecast
        double avgTemp = forecasts.stream().mapToDouble(WeatherForecast::getTemperature).average().orElse(0);
        double avgHumidity = forecasts.stream().mapToDouble(WeatherForecast::getHumidity).average().orElse(0);
        boolean hasRain = forecasts.stream().anyMatch(f -> f.getDescription().toLowerCase().contains("rain"));

        // Create guidance text
        StringBuilder guidance = new StringBuilder();
        guidance.append("Farming Guidance Based on Forecast:\n\n");

        // Temperature guidance
        if (avgTemp > 35) {
            guidance.append("• High temperatures expected: Consider providing shade for sensitive crops and increasing irrigation frequency.\n");
        } else if (avgTemp < 22) {
            guidance.append("• Cooler temperatures expected: Growth may be slower for heat-loving crops. Plan field activities during warmer parts of the day.\n");
        } else {
            guidance.append("• Temperature forecast looks favorable for most crops grown in Ghana.\n");
        }

        // Humidity/rain guidance
        if (avgHumidity > 85) {
            guidance.append("• High humidity levels expected: Monitor crops for fungal diseases, especially vegetables, cocoa, and other susceptible crops.\n");
        }

        if (hasRain) {
            guidance.append("• Rain is expected in the coming days: Consider delaying fertilizer application, pesticide spraying, and harvesting of dry grains.\n");
        } else {
            guidance.append("• Dry conditions expected: Ensure adequate irrigation for crops, especially for recently planted seedlings.\n");
        }

        // Add more context-aware advice
        int currentMonth = LocalDateTime.now().getMonthValue();
        if ((currentMonth >= 3 && currentMonth <= 5) || (currentMonth >= 9 && currentMonth <= 10)) {
            guidance.append("• Current period is typically planting season in southern Ghana: Monitor soil moisture for optimal planting conditions.\n");
        } else if (currentMonth >= 6 && currentMonth <= 8) {
            guidance.append("• This is a key growing period in northern Ghana: Ensure crops have adequate nutrients through appropriate fertilization.\n");
        } else if (currentMonth >= 11 || currentMonth <= 2) {
            guidance.append("• Harmattan season effects may be present: Increase irrigation frequency and consider dust protection for sensitive crops.\n");
        }

        // Create text node and add to chart parent container
        VBox parent = (VBox) tempTrendChart.getParent();

        // Remove previous guidance if it exists
        parent.getChildren().removeIf(node -> node instanceof Text && ((Text) node).getText().startsWith("Farming Guidance"));

        Text guidanceText = new Text(guidance.toString());
        guidanceText.setWrappingWidth(500);
        guidanceText.setStyle("-fx-fill: white;");

        // Add before the second chart
        int chartIndex = parent.getChildren().indexOf(tempTrendChart);
        if (chartIndex >= 0 && chartIndex + 1 < parent.getChildren().size()) {
            parent.getChildren().add(chartIndex + 1, guidanceText);
        } else {
            parent.getChildren().add(guidanceText);
        }
    }

    private void updatePlantingCalendar() {
        if (calendarContainer == null) return;

        calendarContainer.getChildren().clear();

        // Get all crop recommendations
        Map<String, CropWeatherRecommendation> recommendations = WeatherService.getAllCropRecommendations();

        // Get selected month
        String selectedMonth = monthSelector.getValue();
        if (selectedMonth == null) return;

        // Get selected region (if available)
        String selectedRegion = (regionSelector != null) ? regionSelector.getValue() : "All Regions";

        // Create calendar header
        Text calendarTitle = new Text("Planting Calendar for " + selectedMonth +
                (selectedRegion.equals("All Regions") ? "" : " - " + selectedRegion));
        calendarTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        calendarContainer.getChildren().add(calendarTitle);

        // Create scroll pane for crop list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox cropList = new VBox(10);
        cropList.setPadding(new Insets(10));

        // Filter crops suitable for planting in this month
        boolean isNorthernRegion = isNorthernRegion(selectedRegion);

        List<CropWeatherRecommendation> suitableCrops = recommendations.values().stream()
                .filter(crop -> {
                    // Filter by month
                    boolean isInSeason = isPlantingSeasonForMonth(crop.getPlantingSeason(), selectedMonth, isNorthernRegion);

                    // Filter by region if specified
                    if (!selectedRegion.equals("All Regions")) {
                        return isInSeason && isCropSuitableForRegion(crop.getCrop(), selectedRegion);
                    }

                    return isInSeason;
                })
                .collect(Collectors.toList());

        if (suitableCrops.isEmpty()) {
            Text noCropsText = new Text("No recommended crops for planting in " + selectedMonth +
                    (selectedRegion.equals("All Regions") ? "" : " in " + selectedRegion));
            noCropsText.setStyle("-fx-font-style: italic;");
            cropList.getChildren().add(noCropsText);
        } else {
            // Add each suitable crop
            for (CropWeatherRecommendation crop : suitableCrops) {
                HBox cropItem = new HBox(15);
                cropItem.setAlignment(Pos.CENTER_LEFT);
                cropItem.setPadding(new Insets(10));
                cropItem.setStyle("-fx-background-color: #2A2A2A; -fx-background-radius: 5px;");

                FontIcon cropIcon = new FontIcon("fas-seedling");
                cropIcon.setIconSize(24);
                cropIcon.setIconColor(javafx.scene.paint.Color.GREEN);

                VBox cropDetails = new VBox(5);

                Text cropName = new Text(crop.getCrop());
                cropName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

                String plantingSeason = crop.getPlantingSeason();

                // Format planting season based on region, highlight current month
                String formattedSeason = formatSeasonText(plantingSeason, selectedMonth);

                Text seasonText = new Text("Planting Season: " + formattedSeason);
                Text harvestText = new Text("Harvest Season: " + crop.getHarvestSeason());

                cropDetails.getChildren().addAll(cropName, seasonText, harvestText);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button viewButton = new Button("View Details");
                viewButton.setOnAction(e -> showCropDetails(crop));

                cropItem.getChildren().addAll(cropIcon, cropDetails, spacer, viewButton);
                cropList.getChildren().add(cropItem);
            }
        }

        scrollPane.setContent(cropList);
        calendarContainer.getChildren().add(scrollPane);

        // Add seasonal tips
        addSeasonalTips(selectedMonth, isNorthernRegion);
    }

    private void addSeasonalTips(String month, boolean isNorthernRegion) {
        Text tipsHeader = new Text("Seasonal Farming Tips for " + month);
        tipsHeader.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        calendarContainer.getChildren().add(tipsHeader);

        VBox tipsBox = new VBox(10);
        tipsBox.setPadding(new Insets(10));
        tipsBox.setStyle("-fx-background-color: #2A2A2A; -fx-background-radius: 5px;");

        // Provide region-specific tips based on month
        if (isNorthernRegion) {
            // Northern Ghana tips
            int monthIndex = getMonthIndex(month);
            if (monthIndex >= 2 && monthIndex <= 4) { // March-May
                addTip(tipsBox, "Prepare land for the upcoming rainy season.");
                addTip(tipsBox, "Start early planting of drought-resistant varieties.");
                addTip(tipsBox, "Monitor soil moisture carefully as rains may be erratic in early season.");
            } else if (monthIndex >= 5 && monthIndex <= 7) { // June-August
                addTip(tipsBox, "This is the main planting season for northern Ghana.");
                addTip(tipsBox, "Ensure timely weeding to reduce competition for nutrients.");
                addTip(tipsBox, "Apply fertilizers according to crop requirements.");
                addTip(tipsBox, "Monitor for pests which are more active during rainy season.");
            } else if (monthIndex >= 8 && monthIndex <= 9) { // September-October
                addTip(tipsBox, "Late season crops may still be planted.");
                addTip(tipsBox, "Prepare for harvest of early maturing crops.");
                addTip(tipsBox, "Monitor soil moisture as rains begin to reduce.");
            } else { // November-February
                addTip(tipsBox, "This is primarily harvest season for most crops.");
                addTip(tipsBox, "Ensure proper drying and storage of harvested crops.");
                addTip(tipsBox, "Consider irrigated vegetable production in valley bottoms.");
                addTip(tipsBox, "Protect crops from Harmattan winds and dust.");
            }
        } else {
            // Southern Ghana tips
            int monthIndex = getMonthIndex(month);
            if (monthIndex >= 2 && monthIndex <= 5) { // March-June
                addTip(tipsBox, "This is the major rainy season in southern Ghana.");
                addTip(tipsBox, "Ideal time for planting main staple crops like maize, cassava, and plantain.");
                addTip(tipsBox, "Ensure proper drainage systems to prevent waterlogging.");
                addTip(tipsBox, "Watch for fungal diseases during high humidity periods.");
            } else if (monthIndex == 6 || monthIndex == 7) { // July-August
                addTip(tipsBox, "This is the minor dry season in southern Ghana.");
                addTip(tipsBox, "Harvest early planted crops that have matured.");
                addTip(tipsBox, "Prepare land for minor season planting.");
                addTip(tipsBox, "Monitor soil moisture to determine if irrigation is needed.");
            } else if (monthIndex >= 8 && monthIndex <= 10) { // September-November
                addTip(tipsBox, "This is the minor rainy season in southern Ghana.");
                addTip(tipsBox, "Good for planting short-cycle crops and vegetables.");
                addTip(tipsBox, "Apply fertilizers based on crop needs and growth stage.");
                addTip(tipsBox, "Monitor rainfall patterns which can be erratic.");
            } else { // December-February
                addTip(tipsBox, "This is the major dry season with Harmattan conditions.");
                addTip(tipsBox, "Irrigate crops regularly, preferably early morning or evening.");
                addTip(tipsBox, "Mulch to conserve soil moisture and reduce evaporation.");
                addTip(tipsBox, "Consider vegetable production with irrigation.");
            }
        }

        calendarContainer.getChildren().add(tipsBox);
    }

    private void addTip(VBox container, String tipText) {
        HBox tipBox = new HBox(10);
        tipBox.setAlignment(Pos.CENTER_LEFT);

        FontIcon bulletIcon = new FontIcon("fas-check-circle");
        bulletIcon.setIconSize(12);
        bulletIcon.setIconColor(javafx.scene.paint.Color.LIGHTGREEN);

        Text text = new Text(tipText);
        text.setWrappingWidth(400);

        tipBox.getChildren().addAll(bulletIcon, text);
        container.getChildren().add(tipBox);
    }

    private boolean isNorthernRegion(String region) {
        return region.equals("Northern") ||
                region.equals("Upper East") ||
                region.equals("Upper West");
    }

    private String formatSeasonText(String season, String currentMonth) {
        // Format the season text, highlighting the current month
        // Example: "April-June, September-October" with current month "April"
        // becomes "April-June, September-October" with April highlighted

        String result = season;
        if (season.toLowerCase().contains(currentMonth.toLowerCase())) {
            result = season.replaceAll("(?i)" + currentMonth, "<" + currentMonth + ">");
        }

        return result;
    }

    private int getMonthIndex(String month) {
        String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        for (int i = 0; i < months.length; i++) {
            if (months[i].equalsIgnoreCase(month)) {
                return i;
            }
        }

        return 0;
    }

    private boolean isPlantingSeasonForMonth(String seasonText, String month, boolean isNorthernRegion) {
        // Check if the specified month is in the planting season
        // For northern Ghana, we need to check for single rainy season pattern
        // For southern Ghana, we check for both major and minor seasons

        if (seasonText.toLowerCase().contains("year-round")) {
            return true;
        }

        // Check for region-specific patterns
        if (isNorthernRegion) {
            // Northern Ghana season check
            if (seasonText.toLowerCase().contains("northern ghana")) {
                // Check if the month is mentioned in a northern-specific context
                String[] parts = seasonText.split(",");
                for (String part : parts) {
                    part = part.trim().toLowerCase();
                    if (part.contains("northern") && part.contains(month.toLowerCase())) {
                        return true;
                    }
                }
            }
        } else {
            // Southern Ghana season check
            if (seasonText.toLowerCase().contains("southern ghana")) {
                // Check if the month is mentioned in a southern-specific context
                String[] parts = seasonText.split(",");
                for (String part : parts) {
                    part = part.trim().toLowerCase();
                    if (part.contains("southern") && part.contains(month.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        // Generic check without region specification
        return seasonText.toLowerCase().contains(month.toLowerCase());
    }

    private boolean isCropSuitableForRegion(String cropName, String region) {
        // Map crops to regions they grow well in
        if (region.equals("Northern") || region.equals("Upper East") || region.equals("Upper West")) {
            // Crops well-suited for northern regions
            String[] northernCrops = {"maize", "rice", "millet", "sorghum", "groundnut", "cowpea",
                    "yam", "sweet potato", "cassava", "onions"};

            for (String crop : northernCrops) {
                if (cropName.toLowerCase().contains(crop)) {
                    return true;
                }
            }
        } else if (region.equals("Ashanti") || region.equals("Brong-Ahafo")) {
            // Crops well-suited for middle belt
            String[] middleBeltCrops = {"maize", "cassava", "plantain", "cocoa", "yam",
                    "tomatoes", "pepper", "okra", "sweet potato"};

            for (String crop : middleBeltCrops) {
                if (cropName.toLowerCase().contains(crop)) {
                    return true;
                }
            }
        } else if (region.equals("Western") || region.equals("Central") ||
                region.equals("Greater Accra") || region.equals("Eastern") ||
                region.equals("Volta")) {
            // Crops well-suited for southern regions
            String[] southernCrops = {"maize", "cassava", "plantain", "cocoa", "rice",
                    "vegetables", "tomatoes", "pepper", "okra"};

            for (String crop : southernCrops) {
                if (cropName.toLowerCase().contains(crop)) {
                    return true;
                }
            }
        }

        // Some crops grow well everywhere
        String[] universalCrops = {"maize", "cassava"};
        for (String crop : universalCrops) {
            if (cropName.toLowerCase().contains(crop)) {
                return true;
            }
        }

        return false;
    }

    private void showCropDetails(CropWeatherRecommendation crop) {
        // Create a dialog with crop details
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle(crop.getCrop() + " Growing Guide");
        dialog.setHeaderText(crop.getCrop());

        // Create a grid for the details
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // Add details
        grid.add(new Text("Temperature Range:"), 0, 0);
        grid.add(new Text(String.format("%.1f to %.1f°C",
                crop.getMinTemperature(), crop.getMaxTemperature())), 1, 0);

        grid.add(new Text("Optimal Temperature:"), 0, 1);
        grid.add(new Text(String.format("%.1f°C", crop.getOptimalTemperature())), 1, 1);

        grid.add(new Text("Humidity Range:"), 0, 2);
        grid.add(new Text(String.format("%.0f to %.0f%%",
                crop.getMinHumidity(), crop.getMaxHumidity())), 1, 2);

        grid.add(new Text("Optimal Humidity:"), 0, 3);
        grid.add(new Text(String.format("%.0f%%", crop.getOptimalHumidity())), 1, 3);

        grid.add(new Text("Planting Season:"), 0, 4);
        grid.add(new Text(crop.getPlantingSeason()), 1, 4);

        grid.add(new Text("Harvest Season:"), 0, 5);
        grid.add(new Text(crop.getHarvestSeason()), 1, 5);

        Text tipsHeader = new Text("Growing Tips:");
        tipsHeader.setStyle("-fx-font-weight: bold;");
        grid.add(tipsHeader, 0, 6, 2, 1);

        Text tipsText = new Text(crop.getTips());
        tipsText.setWrappingWidth(400);
        grid.add(tipsText, 0, 7, 2, 1);

        // Add nutritional or market value info
        Text marketHeader = new Text("Market Information:");
        marketHeader.setStyle("-fx-font-weight: bold;");
        grid.add(marketHeader, 0, 8, 2, 1);

        Text marketText = new Text(getMarketInfoForCrop(crop.getCrop()));
        marketText.setWrappingWidth(400);
        grid.add(marketText, 0, 9, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private String getMarketInfoForCrop(String cropName) {
        // Provide market information for common Ghanaian crops
        cropName = cropName.toLowerCase();

        if (cropName.contains("maize")) {
            return "Maize is a staple food in Ghana and has strong local demand. Major markets include Techiman, Ejura, and urban centers. Prices typically rise during the lean season (April-July) and fall after harvest. Local varieties are preferred for traditional foods, while improved varieties may fetch premium prices.";
        } else if (cropName.contains("rice")) {
            return "Rice consumption in Ghana is increasing, but local production meets only 40% of demand. Premium prices are available for high-quality local rice. Major rice growing areas include the Volta Region, Northern Region, and Upper East Region. Value addition through proper milling and packaging can increase profits.";
        } else if (cropName.contains("cassava")) {
            return "Cassava has stable demand for food and industrial uses (gari, flour, starch). Processing into gari, kokonte, or cassava flour adds value. The crop is less susceptible to price volatility and can be stored in the ground for flexible harvesting timing based on market prices.";
        } else if (cropName.contains("yam")) {
            return "Yam commands premium prices, especially the 'Pona' variety. Major markets include Techiman, Kumasi, and Accra. Export markets exist in Europe and North America for high-quality yams. Early harvest yams (August-September) typically fetch higher prices.";
        } else if (cropName.contains("plantain")) {
            return "Plantain has strong urban demand and relatively stable prices. Value addition through processing into chips can provide additional income. The crop is susceptible to seasonal gluts that can temporarily depress prices, especially after the major rainy season harvest.";
        } else if (cropName.contains("cocoa")) {
            return "Cocoa is Ghana's main export crop with a guaranteed minimum price set by COCOBOD. Prices follow international market trends. Certified cocoa (organic, fair trade) can fetch premium prices. Recent government initiatives have improved farmgate prices.";
        } else if (cropName.contains("groundnut") || cropName.contains("peanut")) {
            return "Groundnut has both food and oil markets with steady demand. Value addition through processing into paste or oil increases returns. Major markets are in northern cities and Kumasi. Quality and aflatoxin control are important for better prices.";
        } else if (cropName.contains("tomato")) {
            return "Tomatoes have high but volatile prices due to seasonal supply fluctuations. Imports from Burkina Faso affect local market during Ghana's off-season. Major markets include urban centers and processing factories. The crop requires careful timing to avoid market gluts.";
        } else if (cropName.contains("pepper")) {
            return "Pepper (especially hot varieties) has consistent demand in local markets. Dried pepper has a longer shelf life and allows for selling when prices improve. Export opportunities exist for fresh and dried peppers to regional and European markets.";
        } else {
            return "This crop has local market demand in Ghana. Consider value addition through processing, proper storage to sell when prices are higher, and quality control to access premium markets. Check current market prices through agricultural extension officers or the Esoko market information service.";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation Methods
    @FXML
    private void handleDashboardAction() {
        navigateTo("/fxml/main-dashboard.fxml", dashboardButton);
    }

    @FXML
    private void handleProduceAction() {
        navigateTo("/fxml/produce-management.fxml", produceButton);
    }

    @FXML
    private void handleInventoryAction() {
        navigateTo("/fxml/inventory-management.fxml", inventoryButton);
    }

    @FXML
    private void handleSalesAction() {
        navigateTo("/fxml/sales-management.fxml", salesButton);
    }

    @FXML
    private void handleMarketPricesAction() {
        navigateTo("/fxml/market-prices.fxml", marketPricesButton);
    }

    @FXML
    private void handleWeatherAction() {
        // Already on weather screen
        resetNavButtonStyles(dashboardButton, produceButton, inventoryButton, salesButton, marketPricesButton);
        weatherButton.getStyleClass().add("active-nav-item");
    }

    @FXML
    private void handleProfileAction() {
        // Show profile dialog or implement profile view navigation
        showAlert(Alert.AlertType.INFORMATION, "Profile",
                "Profile management features will be available in a future update.");
    }

    @FXML
    private void handleLogoutAction() {
        SessionManager.clearSession();
        navigateTo("/fxml/login.fxml", weatherButton);
    }

    @FXML
    private void handleLocationSuggestions() {
        // Suggest common Ghanaian locations
        ContextMenu contextMenu = new ContextMenu();

        for (String location : GHANA_LOCATIONS) {
            // Create a final copy of the location for use in the lambda
            final String currentLocation = location;

            MenuItem item = new MenuItem(currentLocation);
            item.setOnAction(e -> {
                locationField.setText(currentLocation);  // Use the final copy instead
                handleGetWeatherAction();
            });
            contextMenu.getItems().add(item);
        }

        contextMenu.show(locationField, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    @FXML
    private void handleFarmingTips() {
        // Show general farming tips based on current weather and season
        StringBuilder tips = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();

        tips.append("General Farming Tips for Ghana\n\n");

        // Add seasonal general advice
        if (currentMonth == 12 || currentMonth <= 2) {
            // Harmattan season
            tips.append("Harmattan Season (December-February) Tips:\n");
            tips.append("• Increase irrigation frequency as dry Harmattan winds increase evaporation\n");
            tips.append("• Apply mulch to conserve soil moisture\n");
            tips.append("• Consider early morning or evening irrigation to minimize water loss\n");
            tips.append("• Protect young plants from dust and wind damage\n");
            tips.append("• For vegetables, consider shade netting during peak heat\n\n");
        } else if (currentMonth >= 3 && currentMonth <= 6) {
            // Major rainy season (southern Ghana)
            tips.append("Major Rainy Season (March-June) Tips for Southern Ghana:\n");
            tips.append("• Ensure proper drainage in fields to prevent waterlogging\n");
            tips.append("• Time fertilizer application to avoid heavy rainfall periods\n");
            tips.append("• Apply fungicides preventatively for susceptible crops\n");
            tips.append("• Consider raised beds for vegetable production\n");
            tips.append("• Monitor for increased pest activity during this period\n\n");
        } else if (currentMonth == 7 || currentMonth == 8) {
            // Minor dry season (southern) / Peak rainy season (northern)
            tips.append("July-August Tips:\n");
            tips.append("• In southern Ghana: Monitor soil moisture during this minor dry season\n");
            tips.append("• In northern Ghana: Ensure weeds are controlled during peak growing season\n");
            tips.append("• Apply top dressing fertilizers for crops in active growth stage\n");
            tips.append("• Start preparing for harvest of early maturing crops\n");
            tips.append("• Monitor for pests and diseases that thrive in changing conditions\n\n");
        } else {
            // Minor rainy season (southern) / End of rainy season (northern)
            tips.append("September-November Tips:\n");
            tips.append("• In southern Ghana: Good time for planting short-cycle crops\n");
            tips.append("• In northern Ghana: Prepare for harvesting and post-harvest handling\n");
            tips.append("• Consider relay cropping systems to maximize production\n");
            tips.append("• Prepare storage facilities for upcoming harvests\n");
            tips.append("• Monitor rainfall patterns which can be erratic during this period\n\n");
        }

        // Add advice based on current weather if available
        if (currentWeather != null) {
            tips.append("Based on Current Weather Conditions:\n");

            // Temperature advice
            double temp = currentWeather.getTemperature();
            if (temp > 35) {
                tips.append("• Current high temperature (").append(String.format("%.1f°C", temp))
                        .append("): Increase irrigation frequency and consider providing shade for sensitive crops\n");
            } else if (temp < 22) {
                tips.append("• Current cool temperature (").append(String.format("%.1f°C", temp))
                        .append("): Growth may be slower for heat-loving crops\n");
            } else {
                tips.append("• Current temperature (").append(String.format("%.1f°C", temp))
                        .append(") is favorable for most crops grown in Ghana\n");
            }

            // Humidity advice
            double humidity = currentWeather.getHumidity();
            if (humidity > 85) {
                tips.append("• Current high humidity (").append(String.format("%.0f%%", humidity))
                        .append("): Monitor for fungal diseases, especially in vegetables and cocoa\n");
            } else if (humidity < 40) {
                tips.append("• Current low humidity (").append(String.format("%.0f%%", humidity))
                        .append("): Increase irrigation to prevent water stress\n");
            }

            // Wind advice
            double wind = currentWeather.getWindSpeed();
            if (wind > 8) {
                tips.append("• Current high wind speed (").append(String.format("%.1f m/s", wind))
                        .append("): Consider temporary windbreaks for tall crops\n");
            }

            // Weather description advice
            String desc = currentWeather.getDescription().toLowerCase();
            if (desc.contains("rain") || desc.contains("shower") || desc.contains("drizzle")) {
                tips.append("• Current rain conditions: Avoid applying fertilizers or pesticides\n");
            } else if (desc.contains("clear") || desc.contains("sun")) {
                tips.append("• Current clear conditions: Good time for field activities like weeding and spraying\n");
            }
        }

        // Add market tips
        tips.append("\nMarket Tips:\n");
        tips.append("• Check current market prices using the Market Prices feature in this app\n");
        tips.append("• Consider value addition through processing to increase product shelf life\n");
        tips.append("• Explore cooperative marketing to access better markets and prices\n");
        tips.append("• Time harvests based on market conditions when possible\n");
        tips.append("• For perishable crops, coordinate transportation before harvesting\n");

        // Show in dialog
        Alert tipsDialog = new Alert(Alert.AlertType.INFORMATION);
        tipsDialog.setTitle("Farming Tips for Ghana");
        tipsDialog.setHeaderText("Seasonal Farming Recommendations");

        // Create scrollable content
        TextArea textArea = new TextArea(tips.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(400);
        textArea.setPrefWidth(500);

        tipsDialog.getDialogPane().setContent(textArea);
        tipsDialog.showAndWait();
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) weatherButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error",
                    "Could not navigate to the requested page: " + e.getMessage());
        }
    }

    private void resetNavButtonStyles() {
        dashboardButton.getStyleClass().remove("active-nav-item");
        produceButton.getStyleClass().remove("active-nav-item");
        inventoryButton.getStyleClass().remove("active-nav-item");
        salesButton.getStyleClass().remove("active-nav-item");
        marketPricesButton.getStyleClass().remove("active-nav-item");
        weatherButton.getStyleClass().remove("active-nav-item");
    }
}