package com.example.lkms.data.models;

public class LabNote {

    private int note_id;
    private int experiment_id;
    private String html_content;
    private String timestamp;

    // Constructor đầy đủ
    public LabNote(int note_id, int experiment_id, String html_content, String timestamp) {
        this.note_id = note_id;
        this.experiment_id = experiment_id;
        this.html_content = html_content;
        this.timestamp = timestamp;
    }

    // Constructor để tạo note mới (chưa có note_id)
    public LabNote(int experiment_id, String html_content, String timestamp) {
        this.experiment_id = experiment_id;
        this.html_content = html_content;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getNote_id() {
        return note_id;
    }

    public void setNote_id(int note_id) {
        this.note_id = note_id;
    }

    public int getExperiment_id() {
        return experiment_id;
    }

    public void setExperiment_id(int experiment_id) {
        this.experiment_id = experiment_id;
    }

    public String getHtml_content() {
        return html_content;
    }

    public void setHtml_content(String html_content) {
        this.html_content = html_content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}