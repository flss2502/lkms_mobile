package com.example.lkms.ui.signup;

import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.lkms.R;
import com.example.lkms.ui.login.LoggedInUserView; // Tái sử dụng

public class SignUpViewModel extends ViewModel {

    // Giống như loginFormState trong LoginViewModel
    private MutableLiveData<SignUpFormState> signUpFormState = new MutableLiveData<>();

    // Giống như loginResult trong LoginViewModel
    private MutableLiveData<SignUpResult> signUpResult = new MutableLiveData<>();

    // Thêm LiveData cho trạng thái loading
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;


    // Getter cho LiveData
    LiveData<SignUpFormState> getSignUpFormState() {
        return signUpFormState;
    }

    LiveData<SignUpResult> getSignUpResult() {
        return signUpResult;
    }


    /**
     * Phương thức này được gọi từ Fragment khi người dùng nhấn nút "Sign Up"
     * (Tương tự phương thức login() trong LoginViewModel)
     */
    public void signUp(String firstName, String lastName, String email, String password, String confirmPassword) {
        // Kiểm tra xác thực lần cuối trước khi gọi API
        if (!isFormValid(firstName, lastName, email, password, confirmPassword)) {
            signUpResult.setValue(new SignUpResult(R.string.invalid_signup_form));
            return;
        }

        _isLoading.setValue(true);

        // Mô phỏng cuộc gọi mạng (tương tự logic cũ)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            _isLoading.setValue(false);

            // Giả sử đăng ký thành công
            LoggedInUserView fakeUser = new LoggedInUserView(java.util.UUID.randomUUID().toString(), firstName + " " + lastName);
            signUpResult.setValue(new SignUpResult(fakeUser));

            // --- HOẶC ---
            // Giả sử đăng ký thất bại (ví dụ: email đã tồn tại)
            // signUpResult.setValue(new SignUpResult(R.string.signup_failed_email_exists));

        }, 2000); // Giả lập 2 giây chờ mạng
    }

    /**
     * Được gọi mỗi khi văn bản trong bất kỳ EditText nào thay đổi
     * (Tương tự phương thức loginDataChanged() trong LoginViewModel)
     */
    public void signUpDataChanged(String firstName, String lastName, String email, String password, String confirmPassword) {
        Integer fnError = isNameValid(firstName) ? null : R.string.invalid_first_name;
        Integer lnError = isNameValid(lastName) ? null : R.string.invalid_last_name;
        Integer emailError = isEmailValid(email) ? null : R.string.invalid_username;
        Integer passError = isPasswordValid(password) ? null : R.string.invalid_password;
        Integer confirmPassError = password.equals(confirmPassword) ? null : R.string.invalid_confirm_password;

        if (fnError != null || lnError != null || emailError != null || passError != null || confirmPassError != null) {
            signUpFormState.setValue(new SignUpFormState(fnError, lnError, emailError, passError, confirmPassError));
        } else {
            signUpFormState.setValue(new SignUpFormState(true));
        }
    }

    private boolean isFormValid(String fn, String ln, String e, String p, String cp) {
        return isNameValid(fn) && isNameValid(ln) && isEmailValid(e) && isPasswordValid(p) && p.equals(cp);
    }

    // Các hàm kiểm tra xác thực
    private boolean isNameValid(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private boolean isEmailValid(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}