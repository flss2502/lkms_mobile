package com.example.lkms.ui.protocol_sops;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.data.repository.ProtocolRepository;
import com.example.lkms.data.models.Protocol;

import java.util.ArrayList;
import java.util.List;

public class ProtocolViewModel extends AndroidViewModel {

    private final ProtocolRepository repository;
    private final LiveData<List<Protocol>> allProtocols; // Nguồn dữ liệu thô từ Repository
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    // LiveData cho danh sách đã lọc (mà Fragment sẽ quan sát)
    private final MediatorLiveData<List<Protocol>> filteredProtocols = new MediatorLiveData<>();

    // LiveData để giao tiếp Uri của file đã chọn (từ Fragment sang Dialog)
    private final MutableLiveData<Uri> selectedFileUri = new MutableLiveData<>();

    public ProtocolViewModel(@NonNull Application application) {
        super(application);
        repository = new ProtocolRepository(application);
        allProtocols = repository.getProtocols();

        // Thiết lập Mediator để lọc
        // Nó sẽ chạy lại hàm filterList() khi 1 trong 2 nguồn dữ liệu (allProtocols hoặc searchQuery) thay đổi
        filteredProtocols.addSource(allProtocols, protocols ->
                filterList(protocols, searchQuery.getValue()));

        filteredProtocols.addSource(searchQuery, query ->
                filterList(allProtocols.getValue(), query));
    }

    /**
     * Hàm nội bộ để thực hiện logic lọc
     */
    private void filterList(List<Protocol> protocols, String query) {
        if (protocols == null) {
            filteredProtocols.setValue(new ArrayList<>());
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredProtocols.setValue(protocols); // Trả về tất cả nếu không tìm kiếm
        } else {
            // Thực hiện lọc
            List<Protocol> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();
            for (Protocol p : protocols) {
                if (p.getTitle().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(p);
                }
            }
            filteredProtocols.setValue(filteredList);
        }
    }

    // ===== CÁC HÀM CHO FRAGMENT/DIALOG GỌI =====

    /**
     * Fragment sẽ quan sát (observe) LiveData này
     */
    public LiveData<List<Protocol>> getFilteredProtocols() {
        return filteredProtocols;
    }

    /**
     * Fragment gọi khi người dùng gõ vào thanh tìm kiếm
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    /**
     * Fragment gọi khi người dùng chuyển Tab (SOP/PROTOCOL)
     */
    public void loadProtocolsByType(String type) {
        repository.loadProtocolsByType(type);
    }

    // --- CRUD ---
    public void insert(Protocol protocol) {
        repository.insert(protocol);
    }

    public void update(Protocol protocol) {
        repository.update(protocol);
    }

    public void delete(Protocol protocol) {
        repository.delete(protocol);
    }

    // --- Giao tiếp File Picker (Fragment -> Dialog) ---

    /**
     * ProtocolFragment gọi hàm này sau khi chọn file thành công
     */
    public void setSelectedFileUri(Uri uri) {
        selectedFileUri.setValue(uri);
    }

    /**
     * AddEditProtocolDialogFragment sẽ quan sát (observe) LiveData này
     */
    public LiveData<Uri> getSelectedFileUri() {
        return selectedFileUri;
    }

    /**
     * AddEditProtocolDialogFragment gọi hàm này sau khi đã nhận Uri
     */
    public void clearSelectedFileUri() {
        selectedFileUri.setValue(null);
    }
}