package com.example.lkms.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.data.models.Protocol;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProtocolRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;

    // Sử dụng MutableLiveData để chứa danh sách protocols
    private final MutableLiveData<List<Protocol>> protocolList = new MutableLiveData<>();
    private String currentType = "SOP"; // Mặc định tải SOPs trước

    public ProtocolRepository(Application application) {
        dbHelper = new DatabaseHelper(application.getApplicationContext());
        executor = Executors.newSingleThreadExecutor();
        loadProtocolsByType(currentType); // Tải danh sách ban đầu
    }

    public LiveData<List<Protocol>> getProtocols() {
        return protocolList;
    }

    /**
     * Tải danh sách protocols theo loại (SOP hoặc PROTOCOL)
     */
    public void loadProtocolsByType(String type) {
        this.currentType = type;
        executor.execute(() -> {
            List<Protocol> protocols = dbHelper.getProtocolsByType(type);
            protocolList.postValue(protocols);
        });
    }

    /**
     * Hàm làm mới (refresh) danh sách hiện tại
     */
    private void refreshCurrentList() {
        loadProtocolsByType(this.currentType);
    }

    public void insert(Protocol protocol) {
        executor.execute(() -> {
            dbHelper.addProtocol(protocol);
            refreshCurrentList();
        });
    }

    public void update(Protocol protocol) {
        executor.execute(() -> {
            dbHelper.updateProtocol(protocol);
            refreshCurrentList();
        });
    }

    public void delete(Protocol protocol) {
        executor.execute(() -> {
            dbHelper.deleteProtocol(protocol.getProto_id());
            refreshCurrentList();
        });
    }
}