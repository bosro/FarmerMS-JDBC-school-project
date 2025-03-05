package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.dao.ProduceDAO;
import com.example.jdbc_assignment.models.Produce;
import com.example.jdbc_assignment.services.MarketPriceService;
import com.example.jdbc_assignment.utils.SessionManager;
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
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProduceManagementController implements Initializable {
    @FXML private VBox mainContentVBox;
    @FXML private TableView<Produce> produceTable;
    @FXML private TableColumn<Produce, String> nameColumn;
    @FXML private TableColumn<Produce, String> categoryColumn;
    @FXML private TableColumn<Produce, String> unitColumn;
    @FXML private TableColumn<Produce, String> descriptionColumn;
    @FXML private TableColumn<Produce, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Text userNameText;
    @FXML private Button dashboardButton;
    @FXML private Button produceButton;
    @FXML private Button inventoryButton;
    @FXML private Button salesButton;
    @FXML private Button marketPricesButton;
    @FXML private StackPane produceFormOverlay;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> unitComboBox;
    @FXML private TextArea descriptionField;
    @FXML private Button weatherButton;

    private final ProduceDAO produceDAO = new ProduceDAO();
    private ObservableList<Produce> produceList = FXCollections.observableArrayList();
    private Produce currentProduce; // For editing existing produce
    private boolean isEditMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set username
        userNameText.setText(SessionManager.getCurrentUser().getFullName());

        // Setup table columns
        setupTableColumns();

        // Load produce data
        loadProduceData();

        // Setup search functionality
        setupSearch();

        // Initialize form dropdowns
        initializeFormDropdowns();

        produceTable.prefWidthProperty().bind(mainContentVBox.widthProperty().subtract(40));

        // Add event listener for window resizing to adjust column widths
        produceTable.widthProperty().addListener((obs, oldVal, newVal) -> {
            double tableWidth = newVal.doubleValue();
            // Adjust column widths proportionally
            nameColumn.setPrefWidth(tableWidth * 0.25);
            categoryColumn.setPrefWidth(tableWidth * 0.15);
            unitColumn.setPrefWidth(tableWidth * 0.10);
            descriptionColumn.setPrefWidth(tableWidth * 0.35);
            actionsColumn.setPrefWidth(tableWidth * 0.15);
        });
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unitOfMeasure"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

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
                    Produce produce = getTableView().getItems().get(getIndex());
                    openEditForm(produce);
                });

                // Setup delete button
                FontIcon deleteIcon = new FontIcon("fas-trash-alt");
                deleteIcon.setIconSize(14);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(event -> {
                    Produce produce = getTableView().getItems().get(getIndex());
                    confirmAndDeleteProduce(produce);
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
        produceTable.setItems(produceList);
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                produceTable.setItems(produceList);
            } else {
                ObservableList<Produce> filteredList = FXCollections.observableArrayList();
                for (Produce produce : produceList) {
                    if (produce.getName().toLowerCase().contains(newValue.toLowerCase()) ||
                            produce.getCategory().toLowerCase().contains(newValue.toLowerCase()) ||
                            produce.getDescription().toLowerCase().contains(newValue.toLowerCase())) {
                        filteredList.add(produce);
                    }
                }
                produceTable.setItems(filteredList);
            }
        });
    }

    private void initializeFormDropdowns() {
        // Initialize category dropdown
        categoryComboBox.getItems().addAll(
                "Grains", "Vegetables", "Fruits", "Tubers", "Cash Crops", "Other"
        );

        // Initialize unit dropdown
        unitComboBox.getItems().addAll(
                "kg", "g", "ton", "piece", "bunch", "bag", "crate", "box"
        );
    }

    @FXML
    private void handleWeatherAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/weather.fxml"));
            Parent weatherView = loader.load();

            Stage stage = (Stage) weatherButton.getScene().getWindow();
            Scene scene = new Scene(weatherView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Add this to see the actual error in console
            System.err.println("Error navigating to weather: " + e.getMessage());

            // Show an error dialog to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not navigate to Weather");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleAddProduceAction() {
        // Reset form fields
        nameField.clear();
        categoryComboBox.setValue(null);
        unitComboBox.setValue(null);
        descriptionField.clear();

        // Set form mode
        isEditMode = false;
        currentProduce = null;

        // Show form
        produceFormOverlay.setVisible(true);
        produceFormOverlay.setManaged(true);
    }

    private void openEditForm(Produce produce) {
        // Set form fields
        nameField.setText(produce.getName());
        categoryComboBox.setValue(produce.getCategory());
        unitComboBox.setValue(produce.getUnitOfMeasure());
        descriptionField.setText(produce.getDescription());

        // Set form mode
        isEditMode = true;
        currentProduce = produce;

        // Show form
        produceFormOverlay.setVisible(true);
        produceFormOverlay.setManaged(true);
    }

    @FXML
    private void handleSaveProduceAction() {
        // Validate form
        if (nameField.getText().isEmpty() || categoryComboBox.getValue() == null ||
                unitComboBox.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill all required fields.");
            return;
        }

        if (isEditMode) {
            // Update existing produce
            currentProduce.setName(nameField.getText());
            currentProduce.setCategory(categoryComboBox.getValue());
            currentProduce.setUnitOfMeasure(unitComboBox.getValue());
            currentProduce.setDescription(descriptionField.getText());

            if (produceDAO.updateProduce(currentProduce)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Produce updated successfully.");
                loadProduceData();
                handleCancelAction();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update produce.");
            }
        } else {
            // Create new produce
            Produce newProduce = new Produce(
                    SessionManager.getCurrentUserId(),
                    nameField.getText(),
                    categoryComboBox.getValue(),
                    descriptionField.getText(),
                    unitComboBox.getValue()
            );

            if (produceDAO.addProduce(newProduce)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Produce added successfully.");
                loadProduceData();
                handleCancelAction();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add produce.");
            }
        }
    }

    @FXML
    private void handleCancelAction() {
        produceFormOverlay.setVisible(false);
        produceFormOverlay.setManaged(false);
    }

    private void confirmAndDeleteProduce(Produce produce) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Produce");
        confirmDialog.setContentText("Are you sure you want to delete " + produce.getName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (produceDAO.deleteProduce(produce.getId())) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Produce deleted successfully.");
                    loadProduceData();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete produce.");
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
        // Already on produce page
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
        // Show profile dialog
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
}