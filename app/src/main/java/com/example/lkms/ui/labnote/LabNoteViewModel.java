package com.example.lkms.ui.labnote; // (Hoặc package 'ui.notebook' của bạn)

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

// (Sửa package nếu cần)
import com.example.lkms.data.repository.LabNoteRepository;
import com.example.lkms.data.models.LabNote;

import java.util.List;

public class LabNoteViewModel extends AndroidViewModel {

    private final LabNoteRepository repository;
    private final LiveData<List<LabNote>> allNotes;

    /**
     * SỬA LỖI: Constructor phải nhận experimentId
     */
    public LabNoteViewModel(@NonNull Application application, int experimentId) {
        super(application);
        // Truyền ID xuống Repository
        repository = new LabNoteRepository(application, experimentId);
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<LabNote>> getAllNotes() {
        return allNotes;
    }

    public void saveNote(String htmlContent) {
        repository.saveNote(htmlContent);
    }

    /**
     * SỬA LỖI: Cần một Factory để tạo ViewModel này
     */
    public static class LabNoteViewModelFactory implements ViewModelProvider.Factory {
        private final Application application;
        private final int experimentId;

        public LabNoteViewModelFactory(Application application, int experimentId) {
            this.application = application;
            this.experimentId = experimentId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(LabNoteViewModel.class)) {
                return (T) new LabNoteViewModel(application, experimentId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}