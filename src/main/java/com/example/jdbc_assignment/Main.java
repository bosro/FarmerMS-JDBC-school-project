package com.example.jdbc_assignment;

import com.example.jdbc_assignment.utils.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Test database connection
        DatabaseConnection.testConnection();

        try {
            // Load login screen
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);

            // Add stylesheet
            scene.getStylesheets().add(Main.class.getResource("/styles/dark-theme.css").toExternalForm());

            // Configure stage
            stage.setTitle("Farmers' Market Management System");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error starting application:");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Cleanup resources when application closes
        try {
            // Stop market price update service
            com.example.jdbc_assignment.services.MarketPriceService.stopPriceUpdateService();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}