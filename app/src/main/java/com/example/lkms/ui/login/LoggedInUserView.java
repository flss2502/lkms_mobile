package com.example.lkms.ui.login;

public class LoggedInUserView {
    private String userId;
    private String displayName;

    public LoggedInUserView(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}
