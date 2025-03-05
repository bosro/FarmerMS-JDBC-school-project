package com.example.jdbc_assignment.controllers;

import com.example.jdbc_assignment.utils.WindowManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class BaseController {

    /**
     * Navigate to a different screen while preserving window state
     * @param fxmlPath The path to the FXML file
     * @param sourceNode Any node from the current scene (to get the stage)
     */
    protected void navigateTo(String fxmlPath, Node sourceNode) {
        try {
            // Save current window state
            Stage currentStage = (Stage) sourceNode.getScene().getWindow();
            WindowManager.saveWindowState(currentStage);

            // Load new scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newView = loader.load();

            Scene scene = new Scene(newView);
            scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());

            // Set scene to stage and restore dimensions
            currentStage.setScene(scene);
            WindowManager.applyWindowState(currentStage);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error navigating: " + e.getMessage());

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not navigate to requested screen");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Reset navigation button styles (remove active class)
     * @param buttons The navigation buttons to reset
     */
    protected void resetNavButtonStyles(javafx.scene.control.Button... buttons) {
        for (javafx.scene.control.Button button : buttons) {
            button.getStyleClass().remove("active-nav-item");
        }
    }
}