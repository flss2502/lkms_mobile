package com.example.lkms.data.models;

import java.io.Serializable;

// Thêm "implements Serializable" để có thể gửi qua Dialog
public class Protocol implements Serializable {

    private int proto_id;
    private String title;
    private String type; // "SOP" hoặc "PROTOCOL"
    private int version;
    private String authorId;

    // ===== CÁC TRƯỜNG BỊ THIẾU CỦA BẠN (ĐÃ BỔ SUNG) =====
    private String contentType; // "HTML" hoặc "FILE"
    private String contentData; // Nội dung HTML hoặc URL
    private String contentMimeType; // "application/pdf"

    /**
     * Constructor để tạo mới (chưa có id)
     */
    public Protocol(String title, String type, int version, String authorId, String contentType, String contentData, String contentMimeType) {
        this.title = title;
        this.type = type;
        this.version = version;
        this.authorId = authorId;
        this.contentType = contentType;
        this.contentData = contentData;
        this.contentMimeType = contentMimeType;
    }

    /**
     * Constructor đầy đủ (để đọc từ DB - DatabaseHelper gọi hàm này)
     */
    public Protocol(int proto_id, String title, String type, int version, String authorId, String contentType, String contentData, String contentMimeType) {
        this.proto_id = proto_id;
        this.title = title;
        this.type = type;
        this.version = version;
        this.authorId = authorId;
        this.contentType = contentType;
        this.contentData = contentData;
        this.contentMimeType = contentMimeType;
    }

    // ===== GETTERS (HÀM BỊ THIẾU CỦA BẠN) =====

    public int getProto_id() { return proto_id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public int getVersion() { return version; }
    public String getAuthorId() { return authorId; }

    /**
     * Lỗi "Cannot resolve method" là do thiếu hàm này
     */
    public String getContentType() { return contentType; }

    /**
     * Lỗi "Cannot resolve method" là do thiếu hàm này
     */
    public String getContentData() { return contentData; }

    /**
     * Lỗi "Cannot resolve method" là do thiếu hàm này
     */
    public String getContentMimeType() { return contentMimeType; }

    // ===== SETTERS (Cần cho việc Edit) =====

    public void setProto_id(int proto_id) { this.proto_id = proto_id; }
    public void setTitle(String title) { this.title = title; }
    public void setType(String type) { this.type = type; }
    public void setVersion(int version) { this.version = version; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public void setContentData(String contentData) { this.contentData = contentData; }
    public void setContentMimeType(String contentMimeType) { this.contentMimeType = contentMimeType; }
}