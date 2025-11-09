package com.example.lkms.data.models;

public class BookingDashboard {
    private String equipmentName;
    private String timeSlot;
    private String bookedBy;

    public BookingDashboard(String equipmentName, String timeSlot, String bookedBy) {
        this.equipmentName = equipmentName;
        this.timeSlot = timeSlot;
        this.bookedBy = bookedBy;
    }

    public String getEquipmentName() { return equipmentName; }
    public String getTimeSlot() { return timeSlot; }
    public String getBookedBy() { return bookedBy; }
}