package com.example.lkms.ui.signup;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lkms.data.DatabaseHelper; // <-- BỔ SUNG: Import DatabaseHelper
import com.example.lkms.databinding.FragmentSignUpBinding;
import com.example.lkms.R;
// (Không cần LoggedInUserView ở đây)

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpFragment extends Fragment {

    private SignUpViewModel signUpViewModel;
    private FragmentSignUpBinding binding;

    // (Không cần mAuth ở đây nữa, ViewModel sẽ quản lý)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel VỚI Factory (để truyền DatabaseHelper)
        // (Chúng ta sẽ cần tạo SignUpViewModelFactory)
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        SignUpViewModelFactory factory = new SignUpViewModelFactory(
                FirebaseAuth.getInstance(),
                dbHelper
        );
        signUpViewModel = new ViewModelProvider(this, factory).get(SignUpViewModel.class);

        // Lấy các view từ binding
        final EditText firstNameEditText = binding.firstNameEdittext;
        final EditText lastNameEditText = binding.lastNameEdittext;
        final EditText emailEditText = binding.emailEdittext;
        final EditText passwordEditText = binding.passwordEdittext;
        final EditText confirmPasswordEditText = binding.confirmPasswordEdittext;
        final Button signUpButton = binding.signUpButton;
        final ProgressBar loadingProgressBar = binding.loading;

        // ===== LẮNG NGHE KẾT QUẢ TỪ VIEWMODEL =====

        // 1. Lắng nghe Trạng thái Form (để bật/tắt nút Đăng ký)
        signUpViewModel.getSignUpFormState().observe(getViewLifecycleOwner(), signUpFormState -> {
            if (signUpFormState == null) return;

            signUpButton.setEnabled(signUpFormState.isDataValid());

            // Hiển thị lỗi (nếu có)
            if (signUpFormState.getFirstNameError() != null)
                binding.firstNameLayout.setError(getString(signUpFormState.getFirstNameError()));
            else
                binding.firstNameLayout.setError(null);

            if (signUpFormState.getLastNameError() != null)
                binding.lastNameLayout.setError(getString(signUpFormState.getLastNameError()));
            else
                binding.lastNameLayout.setError(null);

            if (signUpFormState.getEmailError() != null)
                binding.emailLayout.setError(getString(signUpFormState.getEmailError()));
            else
                binding.emailLayout.setError(null);

            if (signUpFormState.getPasswordError() != null)
                binding.passwordLayout.setError(getString(signUpFormState.getPasswordError()));
            else
                binding.passwordLayout.setError(null);

            if (signUpFormState.getConfirmPasswordError() != null)
                binding.confirmPasswordLayout.setError(getString(signUpFormState.getConfirmPasswordError()));
            else
                binding.confirmPasswordLayout.setError(null);
        });

        // 2. Lắng nghe Kết quả Đăng ký (Thành công hay Thất bại)
        signUpViewModel.getSignUpResult().observe(getViewLifecycleOwner(), signUpResult -> {
            if (signUpResult == null) {
                return;
            }
            loadingProgressBar.setVisibility(View.GONE);

            if (signUpResult.getError() != null) {
                // Thất bại
                showSignUpFailed(signUpResult.getError());
            }
            if (signUpResult.getSuccess() != null) {
                // Thành công
                updateUiOnSuccess(signUpResult.getSuccess());

                // Điều hướng về Login
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_signUpFragment_to_loginFragment);
            }
        });

        // 3. Lắng nghe trạng thái loading (để hiển thị ProgressBar)
        signUpViewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                signUpButton.setEnabled(false); // Vô hiệu hóa nút khi đang tải
            } else {
                loadingProgressBar.setVisibility(View.GONE);
                // (Không bật lại nút ở đây, để getSignUpFormState quyết định)
            }
        });

        // ===== GÁN HÀNH ĐỘNG CHO VIEW =====

        // TextWatcher để theo dõi thay đổi (Giữ nguyên)
        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                signUpViewModel.signUpDataChanged(
                        firstNameEditText.getText().toString().trim(),
                        lastNameEditText.getText().toString().trim(),
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString(),
                        confirmPasswordEditText.getText().toString()
                );
            }
        };
        firstNameEditText.addTextChangedListener(afterTextChangedListener);
        lastNameEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener);

        // Bắt sự kiện nhấn "Done" trên bàn phím
        confirmPasswordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && signUpButton.isEnabled()) {
                // Gọi ViewModel để đăng ký
                signUpViewModel.signUp(
                        firstNameEditText.getText().toString().trim(),
                        lastNameEditText.getText().toString().trim(),
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString()
                );
            }
            return false;
        });

        // Bắt sự kiện nhấn nút "Đăng ký"
        signUpButton.setOnClickListener(v -> {
            if (!signUpButton.isEnabled()) return;

            // Gọi ViewModel để đăng ký
            signUpViewModel.signUp(
                    firstNameEditText.getText().toString().trim(),
                    lastNameEditText.getText().toString().trim(),
                    emailEditText.getText().toString().trim(),
                    passwordEditText.getText().toString()
            );
        });

        // Nút Back
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        // Link "Đăng nhập"
        binding.logInLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signUpFragment_to_loginFragment);
        });

        // Nút Google
        binding.googleSignUpButton.setOnClickListener(v ->
                Toast.makeText(getContext(), "Vui lòng đăng nhập bằng Google ở màn hình Đăng nhập", Toast.LENGTH_SHORT).show());
    }

    /**
     * Hiển thị khi đăng ký thành công
     */
    private void updateUiOnSuccess(FirebaseUser user) {
        Toast.makeText(
                getContext(),
                "Đăng ký thành công! Vui lòng kiểm tra email " + user.getEmail() + " để xác thực tài khoản.",
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Hiển thị khi đăng ký thất bại
     */
    private void showSignUpFailed(@StringRes Integer errorString) {
        Toast.makeText(getContext(), getString(errorString), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}