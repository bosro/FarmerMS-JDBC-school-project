package com.example.jdbc_assignment.models;

import java.math.BigDecimal;

public class Sale {
    private int id;
    private int produceId;
    private int userId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String saleDate;
    private String buyerName;
    private String notes;
    private String produceName; // For joining with produce table
    private String unitOfMeasure; // For joining with produce table

    // Default constructor
    public Sale() {}

    // Constructor for new sale
    public Sale(int produceId, int userId, BigDecimal quantity, BigDecimal unitPrice, String buyerName, String notes) {
        this.produceId = produceId;
        this.userId = userId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = quantity.multiply(unitPrice);
        this.buyerName = buyerName;
        this.notes = notes;
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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
        // Update total amount when quantity changes
        if (this.unitPrice != null) {
            this.totalAmount = quantity.multiply(this.unitPrice);
        }
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        // Update total amount when unit price changes
        if (this.quantity != null) {
            this.totalAmount = this.quantity.multiply(unitPrice);
        }
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}