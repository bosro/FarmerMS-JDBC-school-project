package com.example.jdbc_assignment.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import java.io.IOException;
import com.example.jdbc_assignment.dao.UserDAO;
import com.example.jdbc_assignment.models.User;
import com.example.jdbc_assignment.utils.SessionManager;
import com.example.jdbc_assignment.services.MarketPriceService;
import javafx.scene.control.Alert;

public class RegisterController {
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void switchToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            Scene scene = new Scene(loginView);

            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchToDashboard() throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-dashboard.fxml"));
            Parent dashboardView = loader.load();

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            Scene scene = new Scene(dashboardView);

            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error loading dashboard:");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "All fields are required!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
            return;
        }

        if (userDAO.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Error", "Email already exists!");
            return;
        }

        // Create new user
        User user = new User(fullName, email, password);

        if (userDAO.registerUser(user)) {
            showAlert(Alert.AlertType.INFORMATION, "Success", "Registration successful!");

            // Option 1: Direct to login page
            switchToLogin();

            // Option 2: Automatically log in the user and redirect to dashboard
            // Comment out the switchToLogin() above and uncomment below if you want automatic login
            /*
            user = userDAO.loginUser(email, password);
            if (user != null) {
                // Set user in session
                SessionManager.setCurrentUser(user);

                // Start market price update service
                MarketPriceService.startPriceUpdateService();

                try {
                    switchToDashboard();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Error loading dashboard!");
                }
            }
            */
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Registration failed!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}