package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.models.MarketPrice;
import com.example.jdbc_assignment.services.MarketPriceService;
import com.example.jdbc_assignment.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class MarketPricesController implements Initializable {
    @FXML private VBox mainContentVBox;
    @FXML private TableView<MarketPrice> marketPricesTable;
    @FXML private TableColumn<MarketPrice, String> produceNameColumn;
    @FXML private TableColumn<MarketPrice, String> minPriceColumn;
    @FXML private TableColumn<MarketPrice, String> maxPriceColumn;
    @FXML private TableColumn<MarketPrice, String> avgPriceColumn;
    @FXML private TableColumn<MarketPrice, String> marketColumn;
    @FXML private TableColumn<MarketPrice, String> dateColumn;
    @FXML private TextField searchField;
    @FXML private Label lastUpdatedLabel;
    @FXML private Text userNameText;
    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private Button weatherButton;

    private ObservableList<MarketPrice> marketPricesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set user name
        userNameText.setText(SessionManager.getCurrentUser().getFullName());

        // Setup table columns
        setupTableColumns();

        // Load market price data
        loadMarketPriceData();

        // Setup search functionality
        setupSearch();

        marketPricesTable.prefWidthProperty().bind(mainContentVBox.widthProperty().subtract(40));

        // Add event listener for window resizing to adjust column widths
        marketPricesTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            // Adjust column widths proportionally
            produceNameColumn.setPrefWidth(tableWidth * 0.20);
            minPriceColumn.setPrefWidth(tableWidth * 0.15);
            maxPriceColumn.setPrefWidth(tableWidth * 0.15);
            avgPriceColumn.setPrefWidth(tableWidth * 0.15);
            marketColumn.setPrefWidth(tableWidth * 0.15);
            dateColumn.setPrefWidth(tableWidth * 0.15);
        });
    }

    @FXML
    private void handleWeatherAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/weather.fxml"));
            Parent weatherView = loader.load();

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(weatherView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupTableColumns() {
        produceNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduceName()));

        minPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getMinPrice().toString()));

        maxPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getMaxPrice().toString()));

        avgPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getAveragePrice().toString()));

        marketColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMarketName()));

        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDateRecorded()));
    }

    private void loadMarketPriceData() {
        List<MarketPrice> prices = MarketPriceService.fetchCurrentMarketPrices();
        marketPricesList.clear();
        marketPricesList.addAll(prices);
        marketPricesTable.setItems(marketPricesList);

        // Update last updated label
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        lastUpdatedLabel.setText("Last Updated: " + LocalDateTime.now().format(formatter));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                marketPricesTable.setItems(marketPricesList);
            } else {
                ObservableList<MarketPrice> filteredList = FXCollections.observableArrayList();
                for (MarketPrice price : marketPricesList) {
                    if (price.getProduceName().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(price);
                    }
                }
                marketPricesTable.setItems(filteredList);
            }
        });
    }

    @FXML
    private void handleRefreshPricesAction() {
        loadMarketPriceData();
    }

    // Navigation Methods
    @FXML
    private void handleDashboardAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-dashboard.fxml"));
            Parent dashboardView = loader.load();

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(dashboardView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleProduceAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/produce-management.fxml"));
            Parent produceView = loader.load();

            Stage stage = (Stage) produceButton.getScene().getWindow();
            Scene scene = new Scene(produceView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleInventoryAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventory-management.fxml"));
            Parent inventoryView = loader.load();

            Stage stage = (Stage) inventoryButton.getScene().getWindow();
            Scene scene = new Scene(inventoryView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSalesAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/sales-management.fxml"));
            Parent salesView = loader.load();

            Stage stage = (Stage) salesButton.getScene().getWindow();
            Scene scene = new Scene(salesView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMarketPricesAction() {
        // Already on market prices page
        resetNavButtonStyles();
        marketPricesButton.getStyleClass().add("active-nav-item");
    }

    @FXML
    private void handleProfileAction() {
        // Show profile dialog or navigate to profile page
    }

    @FXML
    private void handleLogoutAction() {
        // Clear session
        SessionManager.clearSession();

        // Stop services
        MarketPriceService.stopPriceUpdateService();

        // Navigate to login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) dashboardButton.getScene().getWindow();
            Scene scene = new Scene(loginView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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