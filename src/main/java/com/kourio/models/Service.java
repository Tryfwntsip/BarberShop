package com.kourio.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Service {
    private int id;
    private String name;
    private BigDecimal price;
    private int durationMinutes;
    private String category;
    private boolean isActive;
    private LocalDateTime createdAt;

    // Constructors
    public Service() {
        this.isActive = true;
    }

    public Service(int id, String name, BigDecimal price, int durationMinutes, String category, boolean isActive) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.durationMinutes = durationMinutes;
        this.category = category;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        if (price == null) return name;
        return name + " (" + durationMinutes + " min - €" + price + ")";
    }
}
