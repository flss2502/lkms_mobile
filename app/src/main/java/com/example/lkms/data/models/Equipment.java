package com.example.lkms.data.models;

import java.io.Serializable;

public class Equipment implements Serializable {

    private int equip_id;
    private String name;
    private String location;
    private String category;

    // Constructor để đọc từ DB
    public Equipment(int equip_id, String name, String location, String category) {
        this.equip_id = equip_id;
        this.name = name;
        this.location = location;
        this.category = category;
    }

    // Constructor để tạo (chưa có id)
    public Equipment(String name, String location, String category) {
        this.name = name;
        this.location = location;
        this.category = category;
    }

    // Getters
    public int getEquip_id() { return equip_id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }

    // Setters (cho việc Edit)
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setCategory(String category) { this.category = category; }

    // Dùng cho Spinner/Dropdown
    @Override
    public String toString() {
        return name; // Hiển thị tên khi dùng trong ArrayAdapter
    }
}