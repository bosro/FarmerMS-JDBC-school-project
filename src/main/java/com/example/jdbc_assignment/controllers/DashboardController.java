package com.example.jdbc_assignment.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;  // Add this import
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    @FXML private VBox mainContentVBox;
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private LineChart<String, Number> activityChart;



    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCharts();
        loadData();
    }

    private void setupCharts() {
        // Initialize charts with sample data
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        revenueSeries.getData().add(new XYChart.Data<>("Jan", 15000));
        revenueSeries.getData().add(new XYChart.Data<>("Feb", 18000));
        revenueSeries.getData().add(new XYChart.Data<>("Mar", 22000));
        revenueSeries.getData().add(new XYChart.Data<>("Apr", 24500));

        revenueChart.getData().add(revenueSeries);

        // Setup for activity chart
        XYChart.Series<String, Number> activitySeries = new XYChart.Series<>();
        activitySeries.setName("Active Users");
        activitySeries.getData().add(new XYChart.Data<>("Mon", 1200));
        activitySeries.getData().add(new XYChart.Data<>("Tue", 1350));
        activitySeries.getData().add(new XYChart.Data<>("Wed", 1500));
        activitySeries.getData().add(new XYChart.Data<>("Thu", 1400));
        activitySeries.getData().add(new XYChart.Data<>("Fri", 1600));
        activitySeries.getData().add(new XYChart.Data<>("Sat", 1200));
        activitySeries.getData().add(new XYChart.Data<>("Sun", 1100));

        activityChart.getData().add(activitySeries);
    }

    private void loadData() {
        // Load your actual data here
    }

    // Add initialize methods for other UI elements if needed
    @FXML
    private void initialize() {
        assert revenueChart != null : "fx:id=\"revenueChart\" was not injected: check your FXML file";
        assert activityChart != null : "fx:id=\"activityChart\" was not injected: check your FXML file";
    }
}