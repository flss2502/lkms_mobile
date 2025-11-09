package com.example.lkms.ui.myexperiment;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class ExperimentViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final String uid;

    public ExperimentViewModelFactory(Application application, String uid) {
        this.application = application;
        this.uid = uid;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(NotebookViewModel.class)) {
            return (T) new NotebookViewModel(application, uid);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}