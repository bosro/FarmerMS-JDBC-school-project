package com.example.jdbc_assignment.components;



import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class StatCard extends VBox {
    private final Label titleLabel;
    private final Label valueLabel;
    private final Label trendLabel;

    public StatCard(String title, String value, String trend) {
        titleLabel = new Label(title);
        valueLabel = new Label(value);
        trendLabel = new Label(trend);

        titleLabel.getStyleClass().add("card-title");
        valueLabel.getStyleClass().add("card-value");
        trendLabel.getStyleClass().add("card-trend");

        this.getStyleClass().add("stat-card");
        this.getChildren().addAll(titleLabel, valueLabel, trendLabel);
    }
}
