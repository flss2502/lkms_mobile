package com.example.lkms.data.models;

public class InventoryAlert {
    private String itemName;
    private String reason;

    public InventoryAlert(String itemName, String reason) {
        this.itemName = itemName;
        this.reason = reason;
    }

    public String getItemName() { return itemName; }
    public String getReason() { return reason; }
}
