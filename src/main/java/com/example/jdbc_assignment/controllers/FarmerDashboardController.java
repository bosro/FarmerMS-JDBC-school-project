package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.components.StatCard;
import com.example.jdbc_assignment.dao.InventoryDAO;
import com.example.jdbc_assignment.dao.ProduceDAO;
import com.example.jdbc_assignment.dao.SaleDAO;
import com.example.jdbc_assignment.models.Inventory;
import com.example.jdbc_assignment.models.MarketPrice;
import com.example.jdbc_assignment.models.Produce;
import com.example.jdbc_assignment.models.Sale;
import com.example.jdbc_assignment.services.MarketPriceService;
import com.example.jdbc_assignment.utils.DatabaseConnection;
import com.example.jdbc_assignment.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FarmerDashboardController extends BaseController implements Initializable {
    @FXML private LineChart<String, Number> salesChart;
    @FXML private LineChart<String, Number> inventoryChart;
    @FXML private ComboBox<String> salesPeriodSelector;
    @FXML private ComboBox<String> inventoryPeriodSelector;
    @FXML private Text totalSalesValue;
    @FXML private Text salesTrend;
    @FXML private Text inventoryCountValue;
    @FXML private Text inventoryTrend;
    @FXML private Text produceCountValue;
    @FXML private Text produceTrend;
    @FXML private Text marketPriceValue;
    @FXML private Text marketPriceTrend;
    @FXML private Text userNameText;
    @FXML private TextField searchField;
    @FXML private VBox recentSalesContainer;
    @FXML private VBox marketPricesContainer;
    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private Button weatherButton;

    private final SaleDAO saleDAO = new SaleDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProduceDAO produceDAO = new ProduceDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialize period selectors
        initializeSelectors();

        // Set user name
        if (SessionManager.getCurrentUser() != null) {
            userNameText.setText(SessionManager.getCurrentUser().getFullName());
        }

        // Mark dashboard button as active
        resetNavButtonStyles(produceButton, inventoryButton, salesButton, marketPricesButton, weatherButton);
        dashboardButton.getStyleClass().add("active-nav-item");

        // Setup charts and load data
        setupCharts();
        loadDashboardData();

        // Start market price update service
        MarketPriceService.startPriceUpdateService();
    }

    @FXML
    private void handleWeatherAction() {
        navigateTo("/fxml/weather.fxml", weatherButton);
    }

    private void initializeSelectors() {
        salesPeriodSelector.getItems().addAll("Today", "This Week", "This Month", "This Year");
        salesPeriodSelector.setValue("This Month");
        salesPeriodSelector.setOnAction(e -> loadSalesChartData());

        inventoryPeriodSelector.getItems().addAll("By Category", "By Value", "By Quantity");
        inventoryPeriodSelector.setValue("By Category");
        inventoryPeriodSelector.setOnAction(e -> loadInventoryChartData());
    }

    private void setupCharts() {
        // Clear any existing data
        salesChart.getData().clear();
        inventoryChart.getData().clear();

        // Load chart data
        loadSalesChartData();
        loadInventoryChartData();
    }

    private void loadSalesChartData() {
        salesChart.getData().clear();

        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Sales");

        // This would be dynamic in a real app based on salesPeriodSelector value
        // For now, we'll use sample data
        salesSeries.getData().add(new XYChart.Data<>("Jan 1", 120));
        salesSeries.getData().add(new XYChart.Data<>("Jan 5", 150));
        salesSeries.getData().add(new XYChart.Data<>("Jan 10", 90));
        salesSeries.getData().add(new XYChart.Data<>("Jan 15", 210));
        salesSeries.getData().add(new XYChart.Data<>("Jan 20", 180));
        salesSeries.getData().add(new XYChart.Data<>("Jan 25", 220));
        salesSeries.getData().add(new XYChart.Data<>("Jan 30", 250));

        salesChart.getData().add(salesSeries);
    }

    private void loadInventoryChartData() {
        inventoryChart.getData().clear();

        XYChart.Series<String, Number> inventorySeries = new XYChart.Series<>();
        inventorySeries.setName("Inventory Value");

        // Sample data - would be replaced with real data
        inventorySeries.getData().add(new XYChart.Data<>("Maize", 1500));
        inventorySeries.getData().add(new XYChart.Data<>("Rice", 2300));
        inventorySeries.getData().add(new XYChart.Data<>("Cassava", 800));
        inventorySeries.getData().add(new XYChart.Data<>("Yam", 1200));
        inventorySeries.getData().add(new XYChart.Data<>("Tomatoes", 950));

        inventoryChart.getData().add(inventorySeries);
    }

    private void loadDashboardData() {
        int userId = SessionManager.getCurrentUserId();

        // Load total sales
        // Load total sales
        BigDecimal totalSales = saleDAO.getTotalSalesAmountByUser(userId);
        if (totalSales == null) {
            totalSales = BigDecimal.ZERO;
        }
        totalSalesValue.setText("₵" + totalSales.toString());
        salesTrend.setText("+0%"); // Set to 0% since there are no previous sales to compare


        // Load inventory count
        List<Inventory> inventories = inventoryDAO.getAllInventoryByUser(userId);
        inventoryCountValue.setText(String.valueOf(inventories.size()));
        inventoryTrend.setText("+5%"); // This would be calculated in a real app

        // Load produce count
        List<Produce> produces = produceDAO.getAllProduceByUser(userId);
        produceCountValue.setText(String.valueOf(produces.size()));
        produceTrend.setText("+8%"); // This would be calculated in a real app

        // Load market price average (this is a simplified example)
        List<MarketPrice> prices = MarketPriceService.fetchCurrentMarketPrices();
        if (!prices.isEmpty()) {
            BigDecimal sum = BigDecimal.ZERO;
            for (MarketPrice price : prices) {
                sum = sum.add(price.getAveragePrice());
            }
            BigDecimal average = sum.divide(new BigDecimal(prices.size()), 2, BigDecimal.ROUND_HALF_UP);
            marketPriceValue.setText("₵" + average.toString() + "/kg");
            marketPriceTrend.setText("+3%"); // This would be calculated in a real app
        }

        // Load recent sales
        loadRecentSales(userId);

        // Load market prices
        loadMarketPrices();
    }

    private void loadRecentSales(int userId) {
        recentSalesContainer.getChildren().clear();

        List<Sale> sales = saleDAO.getAllSalesByUser(userId);
        if (sales.isEmpty()) {
            // Add a message when no sales exist
            Text noSalesText = new Text("No sales records found");
            noSalesText.getStyleClass().add("info-text");
            recentSalesContainer.getChildren().add(noSalesText);
            return;
        }

        int count = 0;
        for (Sale sale : sales) {
            if (count >= 3) break; // Show only 3 most recent sales

            HBox saleItem = createSaleItem(sale);
            recentSalesContainer.getChildren().add(saleItem);
            count++;
        }
    }

    private HBox createSaleItem(Sale sale) {
        HBox saleItem = new HBox();
        saleItem.getStyleClass().add("activity-item");
        saleItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        saleItem.setPadding(new javafx.geometry.Insets(15));
        saleItem.setSpacing(15);

        // Icon container
        javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane();
        iconContainer.getStyleClass().add("activity-icon-container");
        FontIcon icon = new FontIcon("fas-shopping-cart");
        icon.setIconSize(16);
        iconContainer.getChildren().add(icon);

        // Sale details
        VBox details = new VBox();

        Text title = new Text(sale.getProduceName() + " Sale");
        title.getStyleClass().add("activity-title");

        Text subtitle = new Text(sale.getQuantity() + " " + sale.getUnitOfMeasure() +
                " sold at ₵" + sale.getUnitPrice() + " each");
        subtitle.getStyleClass().add("activity-subtitle");

        details.getChildren().addAll(title, subtitle);

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Sale time
        Text time = new Text(sale.getSaleDate());
        time.getStyleClass().add("activity-time");

        saleItem.getChildren().addAll(iconContainer, details, spacer, time);

        return saleItem;
    }

    private void loadMarketPrices() {
        marketPricesContainer.getChildren().clear();

        List<MarketPrice> prices = MarketPriceService.fetchCurrentMarketPrices();
        int count = 0;

        for (MarketPrice price : prices) {
            if (count >= 3) break; // Show only 3 market prices

            HBox priceItem = createMarketPriceItem(price);
            marketPricesContainer.getChildren().add(priceItem);
            count++;
        }
    }

    private HBox createMarketPriceItem(MarketPrice price) {
        HBox priceItem = new HBox();
        priceItem.getStyleClass().add("activity-item");
        priceItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        priceItem.setPadding(new javafx.geometry.Insets(15));
        priceItem.setSpacing(15);

        // Icon container
        javafx.scene.layout.StackPane iconContainer = new javafx.scene.layout.StackPane();
        iconContainer.getStyleClass().add("activity-icon-container");
        FontIcon icon = new FontIcon("fas-chart-line");
        icon.setIconSize(16);
        iconContainer.getChildren().add(icon);

        // Price details
        VBox details = new VBox();

        Text title = new Text(price.getProduceName());
        title.getStyleClass().add("activity-title");

        Text subtitle = new Text("Avg: ₵" + price.getAveragePrice() +
                " (Min: ₵" + price.getMinPrice() +
                ", Max: ₵" + price.getMaxPrice() + ")");
        subtitle.getStyleClass().add("activity-subtitle");

        details.getChildren().addAll(title, subtitle);

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Market name and date
        Text market = new Text(price.getMarketName() + " - " + price.getDateRecorded());
        market.getStyleClass().add("activity-time");

        priceItem.getChildren().addAll(iconContainer, details, spacer, market);

        return priceItem;
    }

    @FXML
    private void handleDashboardAction() {
        // Already on dashboard
        resetNavButtonStyles(produceButton, inventoryButton, salesButton, marketPricesButton, weatherButton);
        dashboardButton.getStyleClass().add("active-nav-item");
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


    private void resetNavButtonStyles() {
        dashboardButton.getStyleClass().remove("active-nav-item");
        produceButton.getStyleClass().remove("active-nav-item");
        inventoryButton.getStyleClass().remove("active-nav-item");
        salesButton.getStyleClass().remove("active-nav-item");
        marketPricesButton.getStyleClass().remove("active-nav-item");
        weatherButton.getStyleClass().remove("active-nav-item");
    }

    @FXML
    private void handleProfileAction() {
        // Show profile dialog or navigate to profile page
    }

    @FXML
    private void handleLogoutAction() {
        SessionManager.clearSession();
        navigateTo("/fxml/login.fxml", dashboardButton);
    }


    @FXML
    private void handleRefreshAction() {
        loadDashboardData();
    }

    @FXML
    private void handleSettingsAction() {
        // Show settings dialog
    }

    @FXML
    private void handleViewAllSalesAction() {
        navigateTo("/fxml/sales-management.fxml", dashboardButton);
    }

    @FXML
    private void handleViewAllPricesAction() {
        navigateTo("/fxml/market-prices.fxml", dashboardButton);
    }


}