package com.example.lkms.data.models;

import java.io.Serializable; // <-- THÊM IMPORT NÀY

public class InventoryItem implements Serializable { // <-- THÊM "implements Serializable"

    private int inv_id;
    private String name;
    // ... các trường khác
    private double quantity;
    private String unit;
    private String location;
    private String category;
    private String description;


    // Constructor đầy đủ (để đọc từ DB)
    public InventoryItem(int inv_id, String name, String description, double quantity, String unit, String location, String category) {
        this.inv_id = inv_id;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.category = category;
    }

    // Constructor để tạo mới (chưa có inv_id)
    public InventoryItem(String name, String description, double quantity, String unit, String location, String category) {
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.category = category;
    }

    // Getters
    public int getInv_id() { return inv_id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }

    // Setters (Rất quan trọng cho việc Edit)
    public void setInv_id(int inv_id) { this.inv_id = inv_id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }

}