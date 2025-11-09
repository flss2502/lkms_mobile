package com.example.lkms.data.models;

import java.io.Serializable;

public class Booking implements Serializable {

    private int book_id;
    private int equipmentId;
    private String userId;
    private String startTime; // Lưu dạng TEXT (ví dụ: "2025-11-10T10:00:00")
    private String endTime;
    private String notes;

    // --- Các trường này KHÔNG có trong CSDL ---
    // Chúng chỉ dùng để hiển thị dữ liệu đã JOIN (từ DatabaseHelper)
    private String equipmentName;
    private String userName;

    /**
     * Constructor dùng khi đọc dữ liệu từ Database (đã có ID)
     */
    public Booking(int book_id, int equipmentId, String userId, String startTime, String endTime, String notes) {
        this.book_id = book_id;
        this.equipmentId = equipmentId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    /**
     * Constructor dùng khi TẠO MỚI một lịch đặt (chưa có ID)
     */
    public Booking(int equipmentId, String userId, String startTime, String endTime, String notes) {
        this.equipmentId = equipmentId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    // --- Getters ---
    public int getBook_id() { return book_id; }
    public int getEquipmentId() { return equipmentId; }
    public String getUserId() { return userId; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getNotes() { return notes; }

    // --- Getters/Setters cho các trường hiển thị (lấy từ JOIN) ---

    public String getEquipmentName() {
        // Trả về "N/A" (Không xác định) nếu tên chưa được set (từ hàm JOIN)
        return equipmentName != null ? equipmentName : "N/A";
    }
    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getUserName() {
        return userName != null ? userName : "N/A";
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
}