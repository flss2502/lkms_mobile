package com.example.lkms.ui.myexperiment;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.ui.myexperiment.ExperimentRepository;
import com.example.lkms.data.models.Experiment;

import java.util.ArrayList;
import java.util.List;

public class NotebookViewModel extends AndroidViewModel {

    private final ExperimentRepository repository;
    private final LiveData<List<Experiment>> allExperiments;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Experiment>> filteredExperiments = new MediatorLiveData<>();

    private final MutableLiveData<Experiment> selectedExperiment = new MutableLiveData<>();

    // Constructor nhận UID (từ Factory)
    public NotebookViewModel(@NonNull Application application, @NonNull String uid) {
        super(application);
        repository = new ExperimentRepository(application, uid);
        allExperiments = repository.getAllExperiments();

        // Logic lọc (y hệt InventoryViewModel)
        filteredExperiments.addSource(allExperiments, experiments -> {
            filterList(experiments, searchQuery.getValue());
        });

        filteredExperiments.addSource(searchQuery, query -> {
            filterList(allExperiments.getValue(), query);
        });
    }

    private void filterList(List<Experiment> experiments, String query) {
        if (experiments == null) {
            filteredExperiments.setValue(new ArrayList<>());
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredExperiments.setValue(experiments);
        } else {
            List<Experiment> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();
            for (Experiment exp : experiments) {
                if (exp.getName().toLowerCase().contains(lowerCaseQuery) ||
                        (exp.getStatus() != null && exp.getStatus().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(exp);
                }
            }
            filteredExperiments.setValue(filteredList);
        }
    }

    public LiveData<List<Experiment>> getFilteredExperiments() {
        return filteredExperiments;
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    // CRUD functions
    public void insert(Experiment experiment) {
        repository.insert(experiment);
    }

    public void update(Experiment experiment) {
        repository.update(experiment);
    }

    public void delete(Experiment experiment) {
        repository.delete(experiment);
    }
}
