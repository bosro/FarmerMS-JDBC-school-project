package com.example.jdbc_assignment.models;

public class Produce {
    private int id;
    private int userId;
    private String name;
    private String category;
    private String description;
    private String unitOfMeasure;
    private String createdAt;

    // Default constructor
    public Produce() {}

    // Constructor for creating new produce
    public Produce(int userId, String name, String category, String description, String unitOfMeasure) {
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.description = description;
        this.unitOfMeasure = unitOfMeasure;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}