package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.dao.InventoryDAO;
import com.example.jdbc_assignment.dao.ProduceDAO;
import com.example.jdbc_assignment.models.Inventory;
import com.example.jdbc_assignment.models.Produce;
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
import javafx.scene.control.cell.PropertyValueFactory;
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

public class InventoryManagementController implements Initializable {
    @FXML private VBox mainContentVBox;
    @FXML private TableView<Inventory> inventoryTable;
    @FXML private TableColumn<Inventory, String> produceNameColumn;
    @FXML private TableColumn<Inventory, String> quantityColumn;
    @FXML private TableColumn<Inventory, String> unitColumn;
    @FXML private TableColumn<Inventory, String> unitPriceColumn;
    @FXML private TableColumn<Inventory, String> totalValueColumn;
    @FXML private TableColumn<Inventory, String> lastUpdatedColumn;
    @FXML private TableColumn<Inventory, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Text userNameText;
    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private StackPane inventoryFormOverlay;
    @FXML private Text formTitle;
    @FXML private ComboBox<Produce> produceComboBox;
    @FXML private TextField quantityField;
    @FXML private TextField unitPriceField;
    @FXML private Button weatherButton;

    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final ProduceDAO produceDAO = new ProduceDAO();
    private ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();
    private ObservableList<Produce> produceList = FXCollections.observableArrayList();
    private Inventory currentInventory; // For editing existing inventory
    private boolean isEditMode = false;
    private Map<Integer, Produce> produceMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set user name
        userNameText.setText(SessionManager.getCurrentUser().getFullName());

        // Setup table columns
        setupTableColumns();

        // Load produce data for ComboBox
        loadProduceData();

        // Load inventory data
        loadInventoryData();

        // Setup search functionality
        setupSearch();

        // Setup produce ComboBox display
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

        inventoryTable.prefWidthProperty().bind(mainContentVBox.widthProperty().subtract(40));

// Add event listener for window resizing to adjust column widths
        inventoryTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            // Adjust column widths proportionally
            produceNameColumn.setPrefWidth(tableWidth * 0.15);
            quantityColumn.setPrefWidth(tableWidth * 0.10);
            unitColumn.setPrefWidth(tableWidth * 0.08);
            unitPriceColumn.setPrefWidth(tableWidth * 0.12);
            totalValueColumn.setPrefWidth(tableWidth * 0.15);
            lastUpdatedColumn.setPrefWidth(tableWidth * 0.20);
            actionsColumn.setPrefWidth(tableWidth * 0.15);
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
        produceNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduceName()));

        quantityColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantity().toString()));

        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUnitOfMeasure()));

        unitPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getUnitPrice().toString()));

        totalValueColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty("₵" + cellData.getValue().getTotalValue().toString()));

        lastUpdatedColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getLastUpdated()));

        // Setup actions column with edit and delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttonContainer = new HBox(10);

            {
                // Setup edit button
                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().add("edit-button");
                editButton.setOnAction(event -> {
                    Inventory inventory = getTableView().getItems().get(getIndex());
                    openEditForm(inventory);
                });

                // Setup delete button
                FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                deleteIcon.setIconSize(14);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(event -> {
                    Inventory inventory = getTableView().getItems().get(getIndex());
                    confirmAndDeleteInventory(inventory);
                });

                buttonContainer.getChildren().addAll(editButton, deleteButton);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonContainer);
                }
            }
        });
    }

    private void loadProduceData() {
        int userId = SessionManager.getCurrentUserId();
        List<Produce> produces = produceDAO.getAllProduceByUser(userId);

        produceList.clear();
        produceList.addAll(produces);
        produceComboBox.setItems(produceList);

        // Create map for easy lookup
        produceMap.clear();
        for (Produce produce : produces) {
            produceMap.put(produce.getId(), produce);
        }
    }

    private void loadInventoryData() {
        int userId = SessionManager.getCurrentUserId();
        List<Inventory> inventories = inventoryDAO.getAllInventoryByUser(userId);
        inventoryList.clear();
        inventoryList.addAll(inventories);
        inventoryTable.setItems(inventoryList);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                inventoryTable.setItems(inventoryList);
            } else {
                ObservableList<Inventory> filteredList = FXCollections.observableArrayList();
                for (Inventory inventory : inventoryList) {
                    if (inventory.getProduceName().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(inventory);
                    }
                }
                inventoryTable.setItems(filteredList);
            }
        });
    }

    @FXML
    private void handleAddInventoryAction() {
        // Reset form fields
        produceComboBox.setValue(null);
        quantityField.clear();
        unitPriceField.clear();

        // Set form mode
        isEditMode = false;
        currentInventory = null;
        formTitle.setText("Add to Inventory");

        // Show form
        inventoryFormOverlay.setVisible(true);
        inventoryFormOverlay.setManaged(true);
    }

    private void openEditForm(Inventory inventory) {
        // Set form fields
        Produce produce = produceMap.get(inventory.getProduceId());
        produceComboBox.setValue(produce);
        produceComboBox.setDisable(true); // Can't change produce when editing
        quantityField.setText(inventory.getQuantity().toString());
        unitPriceField.setText(inventory.getUnitPrice().toString());

        // Set form mode
        isEditMode = true;
        currentInventory = inventory;
        formTitle.setText("Update Inventory");

        // Show form
        inventoryFormOverlay.setVisible(true);
        inventoryFormOverlay.setManaged(true);
    }

    @FXML
    private void handleSaveInventoryAction() {
        // Validate form
        if (produceComboBox.getValue() == null ||
                quantityField.getText().isEmpty() ||
                unitPriceField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields.");
            return;
        }

        try {
            BigDecimal quantity = new BigDecimal(quantityField.getText());
            BigDecimal unitPrice = new BigDecimal(unitPriceField.getText());

            if (quantity.compareTo(BigDecimal.ZERO) <= 0 || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Quantity and price must be greater than zero.");
                return;
            }

            if (isEditMode) {
                // Update existing inventory
                currentInventory.setQuantity(quantity);
                currentInventory.setUnitPrice(unitPrice);

                if (inventoryDAO.updateInventory(currentInventory)) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Inventory updated successfully.");
                    loadInventoryData();
                    handleCancelAction();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update inventory.");
                }
            } else {
                // Create new inventory
                Produce selectedProduce = produceComboBox.getValue();

                // Check if inventory already exists for this produce
                Inventory existingInventory = inventoryDAO.getInventoryByProduceId(selectedProduce.getId());

                if (existingInventory != null) {
                    // Ask user if they want to update existing inventory
                    Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmDialog.setTitle("Inventory Exists");
                    confirmDialog.setHeaderText("Update Existing Inventory");
                    confirmDialog.setContentText("Inventory already exists for " + selectedProduce.getName() +
                            ". Do you want to update it?");

                    confirmDialog.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            existingInventory.setQuantity(quantity);
                            existingInventory.setUnitPrice(unitPrice);

                            if (inventoryDAO.updateInventory(existingInventory)) {
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Inventory updated successfully.");
                                loadInventoryData();
                                handleCancelAction();
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update inventory.");
                            }
                        }
                    });
                } else {
                    // Create new inventory
                    Inventory newInventory = new Inventory(
                            selectedProduce.getId(),
                            quantity,
                            unitPrice
                    );

                    if (inventoryDAO.addInventory(newInventory)) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Inventory added successfully.");
                        loadInventoryData();
                        handleCancelAction();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to add inventory.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please enter valid numbers for quantity and price.");
        }
    }

    @FXML
    private void handleCancelAction() {
        inventoryFormOverlay.setVisible(false);
        inventoryFormOverlay.setManaged(false);
        produceComboBox.setDisable(false); // Enable for next time
    }

    private void confirmAndDeleteInventory(Inventory inventory) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Inventory");
        confirmDialog.setContentText("Are you sure you want to delete inventory for " + inventory.getProduceName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (inventoryDAO.deleteInventory(inventory.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Inventory deleted successfully.");
                    loadInventoryData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete inventory.");
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
        // Already on inventory page
        resetNavButtonStyles();
        inventoryButton.getStyleClass().add("active-nav-item");
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