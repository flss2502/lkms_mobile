package com.example.lkms.ui.signup;

import android.util.Log;
import android.util.Patterns;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.lkms.R;
import com.example.lkms.data.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpViewModel extends ViewModel {

    private final MutableLiveData<SignUpFormState> signUpFormState = new MutableLiveData<>();
    private final MutableLiveData<SignUpResult> signUpResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private final FirebaseAuth mAuth;
    private final DatabaseHelper dbHelper;

    // Nhận mAuth và dbHelper từ Factory
    SignUpViewModel(FirebaseAuth mAuth, DatabaseHelper dbHelper) {
        this.mAuth = mAuth;
        this.dbHelper = dbHelper;
    }

    // LiveData (cho Fragment quan sát)
    LiveData<SignUpFormState> getSignUpFormState() { return signUpFormState; }
    LiveData<SignUpResult> getSignUpResult() { return signUpResult; }
    LiveData<Boolean> isLoading() { return isLoading; }

    /**
     * Hàm được gọi bởi Fragment khi nhấn nút Đăng ký
     */
    public void signUp(String firstName, String lastName, String email, String password) {

        // 1. Bắt đầu tải (hiện ProgressBar)
        isLoading.setValue(true);

        String fullName = firstName + " " + lastName;

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {

                            // 2. Cập nhật Tên (DisplayName) trên Firebase
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            user.updateProfile(profileUpdates);

                            // 3. Gửi email xác thực
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (!emailTask.isSuccessful()) {
                                            // (Gửi email thất bại, nhưng đăng ký vẫn thành công)
                                            Log.w("SignUpViewModel", "Không thể gửi email xác thực.", emailTask.getException());
                                        }
                                    });

                            // 4. LƯU VÀO CSDL SQLITE (Quan trọng)
                            // Gán vai trò (role) mặc định là "Researcher"
                            dbHelper.insertOrUpdateUser(user.getUid(), fullName, email, "Researcher");

                            // 5. Gửi kết quả thành công
                            isLoading.setValue(false);
                            signUpResult.setValue(new SignUpResult(user));

                        } else {
                            // (Lỗi hiếm gặp)
                            isLoading.setValue(false);
                            signUpResult.setValue(new SignUpResult(R.string.signup_failed));
                        }
                    } else {
                        // 6. Gửi kết quả thất bại
                        isLoading.setValue(false);
                        // (Bạn có thể thêm logic để dịch lỗi của Firebase sang R.string)
                        signUpResult.setValue(new SignUpResult(R.string.signup_failed));
                    }
                });
    }

    /**
     * Hàm được gọi mỗi khi người dùng gõ phím (để validate)
     */
    public void signUpDataChanged(String firstName, String lastName, String email, String password, String confirmPassword) {
        if (!isFirstNameValid(firstName)) {
            signUpFormState.setValue(new SignUpFormState(R.string.invalid_first_name, null, null, null, null));
        } else if (!isLastNameValid(lastName)) {
            signUpFormState.setValue(new SignUpFormState(null, R.string.invalid_last_name, null, null, null));
        } else if (!isEmailValid(email)) {
            signUpFormState.setValue(new SignUpFormState(null, null, R.string.invalid_username, null, null));
        } else if (!isPasswordValid(password)) {
            signUpFormState.setValue(new SignUpFormState(null, null, null, R.string.invalid_password, null));
        } else if (!isConfirmPasswordValid(password, confirmPassword)) {
            signUpFormState.setValue(new SignUpFormState(null, null, null, null, R.string.invalid_confirm_password));
        } else {
            signUpFormState.setValue(new SignUpFormState(true)); // Dữ liệu hợp lệ
        }
    }

    // --- Các hàm kiểm tra (Validation Helpers) ---
    private boolean isFirstNameValid(String firstName) {
        return firstName != null && firstName.trim().length() > 1;
    }
    private boolean isLastNameValid(String lastName) {
        return lastName != null && lastName.trim().length() > 1;
    }
    private boolean isEmailValid(String email) {
        if (email == null) return false;
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5; // (Mật khẩu phải > 5 ký tự)
    }
    private boolean isConfirmPasswordValid(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }
}