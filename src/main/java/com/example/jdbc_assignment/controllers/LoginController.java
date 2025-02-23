package com.example.jdbc_assignment.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import com.example.jdbc_assignment.dao.UserDAO;
import com.example.jdbc_assignment.models.User;
import com.example.jdbc_assignment.utils.SessionManager;
import com.example.jdbc_assignment.services.MarketPriceService;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email and password are required!");
            return;
        }

        User user = userDAO.loginUser(email, password);

        if (user != null) {
            // Set user in session
            SessionManager.setCurrentUser(user);

            // Start market price update service
            MarketPriceService.startPriceUpdateService();

            // Login successful
            try {
                switchToDashboard();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Error loading dashboard!");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid email or password!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void switchToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
            Parent registerView = loader.load();

            // Get the current stage
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(registerView);

            // Add the stylesheet
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-dashboard.fxml"));
        Parent dashboardView = loader.load();

        Stage stage = (Stage) emailField.getScene().getWindow();
        Scene scene = new Scene(dashboardView);

        scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }
}