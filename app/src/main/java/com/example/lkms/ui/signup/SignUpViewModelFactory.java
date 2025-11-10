package com.example.lkms.ui.signup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.lkms.data.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Bắt buộc phải có Factory này để tạo SignUpViewModel
 * vì ViewModel cần 2 tham số (mAuth và dbHelper)
 */
public class SignUpViewModelFactory implements ViewModelProvider.Factory {

    private final FirebaseAuth mAuth;
    private final DatabaseHelper dbHelper;

    public SignUpViewModelFactory(FirebaseAuth mAuth, DatabaseHelper dbHelper) {
        this.mAuth = mAuth;
        this.dbHelper = dbHelper;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SignUpViewModel.class)) {
            return (T) new SignUpViewModel(mAuth, dbHelper);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}