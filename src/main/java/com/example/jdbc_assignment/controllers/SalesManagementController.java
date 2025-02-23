package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.dao.InventoryDAO;
import com.example.jdbc_assignment.dao.ProduceDAO;
import com.example.jdbc_assignment.dao.SaleDAO;
import com.example.jdbc_assignment.models.Inventory;
import com.example.jdbc_assignment.models.MarketPrice;
import com.example.jdbc_assignment.models.Produce;
import com.example.jdbc_assignment.models.Sale;
import com.example.jdbc_assignment.services.MarketPriceService;
import com.example.jdbc_assignment.utils.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SalesManagementController implements Initializable {
    @FXML private VBox mainContentVBox;
    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, String> dateColumn;
    @FXML private TableColumn<Sale, String> produceNameColumn;
    @FXML private TableColumn<Sale, String> quantityColumn;
    @FXML private TableColumn<Sale, String> unitColumn;
    @FXML private TableColumn<Sale, String> priceColumn;
    @FXML private TableColumn<Sale, String> totalColumn;
    @FXML private TableColumn<Sale, String> buyerColumn;
    @FXML private TableColumn<Sale, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Text userNameText;
    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private StackPane saleFormOverlay;
    @FXML private Text formTitle;
    @FXML private ComboBox<Produce> produceComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private TextField buyerNameField;
    @FXML private TextArea notesField;
    @FXML private Label availableQuantityLabel;
    @FXML private Label marketPriceLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Button weatherButton;

    private final SaleDAO saleDAO = new SaleDAO();
    private final ProduceDAO produceDAO = new ProduceDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private ObservableList<Sale> salesList = FXCollections.observableArrayList();
    private ObservableList<Produce> produceList = FXCollections.observableArrayList();
    private Map<Integer, Inventory> inventoryMap = new HashMap<>();
    private Map<String, MarketPrice> marketPriceMap = new HashMap<>();
    private Sale currentSale; // For editing existing sale
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set user name
        userNameText.setText(SessionManager.getCurrentUser().getFullName());

        // Setup table columns
        setupTableColumns();

        // Load sales data
        loadSalesData();

        // Load produce data
        loadProduceData();

        // Load market price data
        loadMarketPriceData();

        // Setup search functionality
        setupSearch();

        // Setup produce ComboBox display
        setupProduceComboBox();

        // Setup quantity and price field listeners for total calculation
        setupFieldListeners();

        // Add this in your initialize() method instead of what you have
        // In initialize method
        salesTable.prefWidthProperty().bind(mainContentVBox.widthProperty().subtract(40));

// Add event listener for window resizing to adjust column widths
        salesTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            // Adjust column widths proportionally
            dateColumn.setPrefWidth(tableWidth * 0.10);
            produceNameColumn.setPrefWidth(tableWidth * 0.15);
            quantityColumn.setPrefWidth(tableWidth * 0.10);
            unitColumn.setPrefWidth(tableWidth * 0.08);
            priceColumn.setPrefWidth(tableWidth * 0.12);
            totalColumn.setPrefWidth(tableWidth * 0.12);
            buyerColumn.setPrefWidth(tableWidth * 0.18);
            actionsColumn.setPrefWidth(tableWidth * 0.10);
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
        dateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSaleDate()));

        produceNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getProduceName()));

        quantityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantity().toString()));

        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUnitOfMeasure()));

        priceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getUnitPrice().toString()));

        totalColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getTotalAmount().toString()));

        buyerColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBuyerName()));

        // Setup actions column with delete button
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();

            {
                // Setup delete button
                FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                deleteIcon.setIconSize(14);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(event -> {
                    Sale sale = getTableView().getItems().get(getIndex());
                    confirmAndDeleteSale(sale);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
    }

    private void loadSalesData() {
        int userId = SessionManager.getCurrentUserId();
        List<Sale> sales = saleDAO.getAllSalesByUser(userId);
        salesList.clear();
        salesList.addAll(sales);
        salesTable.setItems(salesList);
    }

    private void loadProduceData() {
        int userId = SessionManager.getCurrentUserId();
        List<Produce> produces = produceDAO.getAllProduceByUser(userId);

        produceList.clear();
        produceList.addAll(produces);
        produceComboBox.setItems(produceList);

        // Load inventory data for each produce
        inventoryMap.clear();
        for (Produce produce : produces) {
            Inventory inventory = inventoryDAO.getInventoryByProduceId(produce.getId());
            if (inventory != null) {
                inventoryMap.put(produce.getId(), inventory);
            }
        }
    }

    private void loadMarketPriceData() {
        List<MarketPrice> prices = MarketPriceService.fetchCurrentMarketPrices();
        marketPriceMap.clear();

        for (MarketPrice price : prices) {
            marketPriceMap.put(price.getProduceName().toLowerCase(), price);
        }
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                salesTable.setItems(salesList);
            } else {
                ObservableList<Sale> filteredList = FXCollections.observableArrayList();
                for (Sale sale : salesList) {
                    if (sale.getProduceName().toLowerCase().contains(newValue.toLowerCase()) ||
                            sale.getBuyerName().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(sale);
                    }
                }
                salesTable.setItems(filteredList);
            }
        });
    }

    private void setupProduceComboBox() {
        produceComboBox.setCellFactory(param -> new ListCell<Produce>() {
            @Override
            protected void updateItem(Produce item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getUnitOfMeasure() + ")");
                }
            }
        });

        produceComboBox.setButtonCell(new ListCell<Produce>() {
            @Override
            protected void updateItem(Produce item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getUnitOfMeasure() + ")");
                }
            }
        });

        // When produce selection changes, update available quantity and market price
        produceComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Inventory inventory = inventoryMap.get(newValue.getId());
                if (inventory != null) {
                    availableQuantityLabel.setText("Available: " + inventory.getQuantity() + " " + newValue.getUnitOfMeasure());
                    unitPriceField.setText(inventory.getUnitPrice().toString());
                } else {
                    availableQuantityLabel.setText("Available: 0 " + newValue.getUnitOfMeasure());
                    unitPriceField.setText("");
                }

                // Set market price if available
                MarketPrice marketPrice = marketPriceMap.get(newValue.getName().toLowerCase());
                if (marketPrice != null) {
                    marketPriceLabel.setText("Market: ₵" + marketPrice.getAveragePrice());
                } else {
                    marketPriceLabel.setText("Market: N/A");
                }
            } else {
                availableQuantityLabel.setText("Available: 0");
                marketPriceLabel.setText("Market: N/A");
            }

            // Update total
            updateTotalAmount();
        });
    }

    private void setupFieldListeners() {
        // Add listeners to quantity and unit price fields to calculate total
        quantityField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTotalAmount();
        });

        unitPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateTotalAmount();
        });
    }

    private void updateTotalAmount() {
        try {
            if (!quantityField.getText().isEmpty() && !unitPriceField.getText().isEmpty()) {
                BigDecimal quantity = new BigDecimal(quantityField.getText());
                BigDecimal unitPrice = new BigDecimal(unitPriceField.getText());
                BigDecimal total = quantity.multiply(unitPrice);
                totalAmountLabel.setText(total.toString());
            } else {
                totalAmountLabel.setText("0.00");
            }
        } catch (NumberFormatException e) {
            totalAmountLabel.setText("Invalid");
        }
    }

    @FXML
    private void handleRecordSaleAction() {
        // Reset form fields
        produceComboBox.setValue(null);
        quantityField.clear();
        unitPriceField.clear();
        buyerNameField.clear();
        notesField.clear();
        totalAmountLabel.setText("0.00");

        // Set form mode
        isEditMode = false;
        currentSale = null;
        formTitle.setText("Record Sale");

        // Show form
        saleFormOverlay.setVisible(true);
        saleFormOverlay.setManaged(true);
    }

    @FXML
    private void handleSaveSaleAction() {
        // Validate form
        if (produceComboBox.getValue() == null ||
                quantityField.getText().isEmpty() ||
                unitPriceField.getText().isEmpty() ||
                buyerNameField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields.");
            return;
        }

        try {
            Produce selectedProduce = produceComboBox.getValue();
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText());
            String buyerName = buyerNameField.getText();
            String notes = notesField.getText();

            if (quantity.compareTo(BigDecimal.ZERO) <= 0 || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Quantity and price must be greater than zero.");
                return;
            }

            // Check if inventory has enough quantity
            Inventory inventory = inventoryMap.get(selectedProduce.getId());
            if (inventory == null || inventory.getQuantity().compareTo(quantity) < 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Not enough inventory available for this sale.");
                return;
            }

            // Create and save the sale
            Sale newSale = new Sale(
                    selectedProduce.getId(),
                    SessionManager.getCurrentUserId(),
                    quantity,
                    unitPrice,
                    buyerName,
                    notes
            );

            if (saleDAO.addSale(newSale)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Sale recorded successfully.");

                // Refresh inventory map after sale
                Inventory updatedInventory = inventoryDAO.getInventoryByProduceId(selectedProduce.getId());
                inventoryMap.put(selectedProduce.getId(), updatedInventory);

                loadSalesData();
                handleCancelAction();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to record sale.");
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers for quantity and price.");
        }
    }

    @FXML
    private void handleCancelAction() {
        saleFormOverlay.setVisible(false);
        saleFormOverlay.setManaged(false);
    }

    private void confirmAndDeleteSale(Sale sale) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Sale Record");
        confirmDialog.setContentText("Are you sure you want to delete this sale record? This cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (saleDAO.deleteSale(sale.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Sale record deleted successfully.");
                    loadSalesData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete sale record.");
                }
            }
        });
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
        // Already on sales page
        resetNavButtonStyles();
        salesButton.getStyleClass().add("active-nav-item");
    }

    @FXML
    private void handleMarketPricesAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/market-prices.fxml"));
            Parent pricesView = loader.load();

            Stage stage = (Stage) marketPricesButton.getScene().getWindow();
            Scene scene = new Scene(pricesView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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