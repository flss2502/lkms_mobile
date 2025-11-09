package com.example.lkms.ui.myexperiment;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.data.models.Experiment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExperimentRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final String uid; // <-- Cần UID để lọc
    private final MutableLiveData<List<Experiment>> allExperiments = new MutableLiveData<>();

    // Constructor nhận UID
    public ExperimentRepository(Application application, String uid) {
        this.dbHelper = new DatabaseHelper(application.getApplicationContext());
        this.executor = Executors.newSingleThreadExecutor();
        this.uid = uid;
        loadAllExperiments(); // Tải dữ liệu ngay khi khởi tạo
    }

    public LiveData<List<Experiment>> getAllExperiments() {
        return allExperiments;
    }

    public void loadAllExperiments() {
        executor.execute(() -> {
            // Chỉ lấy các thí nghiệm của người dùng này
            List<Experiment> experiments = dbHelper.getExperimentsByUser(this.uid);
            allExperiments.postValue(experiments);
        });
    }

    public void insert(Experiment experiment) {
        executor.execute(() -> {
            // Thêm "createdBy" (uid) khi chèn
            dbHelper.addExperiment(experiment, this.uid);
            loadAllExperiments(); // Tải lại
        });
    }

    public void update(Experiment experiment) {
        executor.execute(() -> {
            dbHelper.updateExperiment(experiment);
            loadAllExperiments(); // Tải lại
        });
    }

    public void delete(Experiment experiment) {
        executor.execute(() -> {
            dbHelper.deleteExperiment(experiment.getId());
            loadAllExperiments(); // Tải lại
        });
    }
}