package com.example.lkms.ui.signup;

import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseUser;

/**
 * Kết quả: Hoặc là thành công (FirebaseUser) hoặc là thất bại (Integer báo lỗi)
 */
class SignUpResult {
    @Nullable
    private FirebaseUser success;
    @Nullable
    private Integer error;

    SignUpResult(@Nullable Integer error) {
        this.error = error;
    }

    SignUpResult(@Nullable FirebaseUser success) {
        this.success = success;
    }

    @Nullable
    FirebaseUser getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}