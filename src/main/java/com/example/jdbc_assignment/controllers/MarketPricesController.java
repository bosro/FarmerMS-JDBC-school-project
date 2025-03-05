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

public class MarketPricesController extends BaseController implements Initializable {
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

        marketPricesTable.setId("marketPricesTable");

        // Make sure the table takes up available width
        marketPricesTable.prefWidthProperty().bind(mainContentVBox.widthProperty().subtract(40));

        // Add this to ensure row height is appropriate
        marketPricesTable.setFixedCellSize(45);

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
    private void handleProfileAction() {
        // Simple implementation that won't cause errors
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Profile");
        alert.setHeaderText("Profile Feature");
        alert.setContentText("Profile functionality will be implemented in a future update.");
        alert.showAndWait();
    }

    @FXML
    private void handleWeatherAction() {
        // Use the helper method from BaseController
        navigateTo("/fxml/weather.fxml", weatherButton);
    }

    private void setupTableColumns() {
        // Set up cell value factories
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

        // Set specific column widths and style each column
        produceNameColumn.setPrefWidth(200);
        minPriceColumn.setPrefWidth(120);
        maxPriceColumn.setPrefWidth(120);
        avgPriceColumn.setPrefWidth(150);
        marketColumn.setPrefWidth(180);
        dateColumn.setPrefWidth(130);

        // Set alignment for price columns (right-aligned)
        minPriceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        maxPriceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
        avgPriceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");

        // Make table columns resize with the table
        marketPricesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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
        // Already on market prices page
        resetNavButtonStyles(dashboardButton, produceButton, inventoryButton, salesButton, weatherButton);
        marketPricesButton.getStyleClass().add("active-nav-item");
    }

    @FXML
    private void handleLogoutAction() {
        // Clear session
        SessionManager.clearSession();

        // Stop services if needed
        MarketPriceService.stopPriceUpdateService();

        // Navigate to login
        navigateTo("/fxml/login.fxml", dashboardButton);
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