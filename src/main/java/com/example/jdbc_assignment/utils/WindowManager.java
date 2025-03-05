package com.example.jdbc_assignment.utils;

import javafx.stage.Stage;

public class WindowManager {
    private static double width = 1200; // Default width
    private static double height = 800; // Default height
    private static boolean isMaximized = false;
    private static boolean isFullScreen = false;

    public static void saveWindowState(Stage stage) {
        if (!stage.isMaximized() && !stage.isFullScreen()) {
            width = stage.getWidth();
            height = stage.getHeight();
        }
        isMaximized = stage.isMaximized();
        isFullScreen = stage.isFullScreen();
    }

    public static void applyWindowState(Stage stage) {
        // Set size first
        stage.setWidth(width);
        stage.setHeight(height);

        // Then apply maximized/fullscreen state
        stage.setMaximized(isMaximized);
        stage.setFullScreen(isFullScreen);
    }
}