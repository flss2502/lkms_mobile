package com.example.lkms.data.repository; // (Hoặc package 'data' của bạn)

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.data.models.LabNote;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LabNoteRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final MutableLiveData<List<LabNote>> notesList = new MutableLiveData<>();

    // ===== SỬA LỖI: Thêm biến experimentId =====
    private final int experimentId;

    /**
     * SỬA LỖI: Constructor phải nhận experimentId
     */
    public LabNoteRepository(Application application, int experimentId) {
        this.dbHelper = new DatabaseHelper(application.getApplicationContext());
        this.executor = Executors.newSingleThreadExecutor();
        this.experimentId = experimentId; // Gán ID
        loadAllNotes(); // Tải dữ liệu khi khởi tạo
    }

    public LiveData<List<LabNote>> getAllNotes() {
        return notesList;
    }

    public void loadAllNotes() {
        executor.execute(() -> {
            // ===== SỬA LỖI 1 Ở ĐÂY =====
            // Gọi hàm lấy notes theo ID (đã có trong DatabaseHelper)
            List<LabNote> notes = dbHelper.getNotesForExperiment(this.experimentId);
            notesList.postValue(notes);
        });
    }

    public void saveNote(String htmlContent) {
        executor.execute(() -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // ===== SỬA LỖI: Dùng "this.experimentId" =====
            dbHelper.saveLabNote(this.experimentId, htmlContent, timestamp);

            // Sau khi lưu, tải lại danh sách
            loadAllNotes();
        });
    }
}