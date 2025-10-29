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

// SỬA LẠI TẠI ĐÂY: Dùng tên class binding khớp với tên file XML (ví dụ: fragment_sign_up.xml -> FragmentSignUpBinding)
import com.example.lkms.databinding.FragmentSignUpBinding;
import com.example.lkms.R;
import com.example.lkms.ui.login.LoggedInUserView;

public class SignUpFragment extends Fragment {

    private SignUpViewModel signUpViewModel;

    // SỬA LẠI TẠI ĐÂY:
    private FragmentSignUpBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // SỬA LẠI TẠI ĐÂY:
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel (Giữ nguyên)
        signUpViewModel = new ViewModelProvider(this).get(SignUpViewModel.class);

        // Lấy các view từ binding (Code này đã khớp 100% với ID trong XML của bạn)
        final EditText firstNameEditText = binding.firstNameEdittext;
        final EditText lastNameEditText = binding.lastNameEdittext;
        final EditText emailEditText = binding.emailEdittext;
        final EditText passwordEditText = binding.passwordEdittext;
        final EditText confirmPasswordEditText = binding.confirmPasswordEdittext;
        final Button signUpButton = binding.signUpButton;
        final ProgressBar loadingProgressBar = binding.loading; // Sẽ hoạt động sau khi bạn thêm ProgressBar vào XML

        signUpViewModel.getSignUpFormState().observe(getViewLifecycleOwner(), new Observer<SignUpFormState>() {
            @Override
            public void onChanged(@Nullable SignUpFormState signUpFormState) {
                if (signUpFormState == null) return;

                signUpButton.setEnabled(signUpFormState.isDataValid());

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
            }
        });

        signUpViewModel.getSignUpResult().observe(getViewLifecycleOwner(), new Observer<SignUpResult>() {
            @Override
            public void onChanged(@Nullable SignUpResult signUpResult) {
                if (signUpResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (signUpResult.getError() != null) {
                    showSignUpFailed(signUpResult.getError());
                }
                if (signUpResult.getSuccess() != null) {
                    updateUiOnSuccess(signUpResult.getSuccess());
                }
            }
        });

        // Lắng nghe trạng thái loading (Giữ nguyên)
        signUpViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                signUpButton.setEnabled(false); // Vô hiệu hóa nút khi đang tải
            } else {
                loadingProgressBar.setVisibility(View.GONE);
                signUpButton.setEnabled(true); // Kích hoạt lại nút
            }
        });

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
                        passwordEditText.getText().toString(), // Không trim mật khẩu
                        confirmPasswordEditText.getText().toString()
                );
            }
        };
        firstNameEditText.addTextChangedListener(afterTextChangedListener);
        lastNameEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        confirmPasswordEditText.addTextChangedListener(afterTextChangedListener);

        // Xử lý khi nhấn "Done" trên bàn phím (Giữ nguyên)
        confirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && signUpButton.isEnabled()) {
                    signUpViewModel.signUp(
                            firstNameEditText.getText().toString().trim(),
                            lastNameEditText.getText().toString().trim(),
                            emailEditText.getText().toString().trim(),
                            passwordEditText.getText().toString(),
                            confirmPasswordEditText.getText().toString()
                    );
                }
                return false;
            }
        });

        // Xử lý sự kiện nút "Sign Up" (Giữ nguyên)
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // loadingProgressBar.setVisibility(View.VISIBLE); // ViewModel tự xử lý việc này
                signUpViewModel.signUp(
                        firstNameEditText.getText().toString().trim(),
                        lastNameEditText.getText().toString().trim(),
                        emailEditText.getText().toString().trim(),
                        passwordEditText.getText().toString(),
                        confirmPasswordEditText.getText().toString()
                );
            }
        });

        // Các sự kiện click khác (Giữ nguyên)
        binding.backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        binding.logInLink.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_signUpFragment_to_loginFragment);
        });

        binding.googleSignUpButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Google Sign Up Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUiOnSuccess(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        Toast.makeText(getContext(), welcome, Toast.LENGTH_LONG).show();
        // TODO: Điều hướng đến màn hình chính
    }

    private void showSignUpFailed(@StringRes Integer errorString) {
        Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}