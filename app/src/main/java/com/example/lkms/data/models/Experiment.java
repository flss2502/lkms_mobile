package com.example.lkms.data.models;

import java.io.Serializable; // <-- THÊM IMPORT NÀY

public class Experiment implements Serializable { // <-- THÊM "implements Serializable"

    private int id;
    private String name;
    private String status;
    private String dueDate;

    // Constructor để tạo mới (chưa có id)
    public Experiment(String name, String status, String dueDate) {
        this.name = name;
        this.status = status;
        this.dueDate = dueDate;
    }

    // Constructor để đọc từ DB (đã có id)
    public Experiment(int id, String name, String status, String dueDate) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.dueDate = dueDate;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getDueDate() {
        return dueDate;
    }

    // Setters (Rất quan trọng cho việc Edit)
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}