package com.example.jdbc_assignment.models;

import java.math.BigDecimal;

public class Inventory {
    private int id;
    private int produceId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private String lastUpdated;
    private String produceName; // For joining with produce table
    private String unitOfMeasure; // For joining with produce table

    // Default constructor
    public Inventory() {}

    // Constructor for new inventory
    public Inventory(int produceId, BigDecimal quantity, BigDecimal unitPrice) {
        this.produceId = produceId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduceId() {
        return produceId;
    }

    public void setProduceId(int produceId) {
        this.produceId = produceId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getProduceName() {
        return produceName;
    }

    public void setProduceName(String produceName) {
        this.produceName = produceName;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    // Calculate total value
    public BigDecimal getTotalValue() {
        return quantity.multiply(unitPrice);
    }
}