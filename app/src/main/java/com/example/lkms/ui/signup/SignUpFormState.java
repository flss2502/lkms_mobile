package com.example.lkms.ui.signup;

import androidx.annotation.Nullable;

/**
 * Trạng thái của Form (dữ liệu có hợp lệ hay không)
 */
class SignUpFormState {
    @Nullable
    private Integer firstNameError;
    @Nullable
    private Integer lastNameError;
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer passwordError;
    @Nullable
    private Integer confirmPasswordError;
    private boolean isDataValid;

    SignUpFormState(boolean isDataValid) {
        this.firstNameError = null;
        this.lastNameError = null;
        this.emailError = null;
        this.passwordError = null;
        this.confirmPasswordError = null;
        this.isDataValid = isDataValid;
    }

    SignUpFormState(@Nullable Integer firstNameError, @Nullable Integer lastNameError, @Nullable Integer emailError, @Nullable Integer passwordError, @Nullable Integer confirmPasswordError) {
        this.firstNameError = firstNameError;
        this.lastNameError = lastNameError;
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.confirmPasswordError = confirmPasswordError;
        this.isDataValid = false;
    }

    @Nullable
    Integer getFirstNameError() { return firstNameError; }
    @Nullable
    Integer getLastNameError() { return lastNameError; }
    @Nullable
    Integer getEmailError() { return emailError; }
    @Nullable
    Integer getPasswordError() { return passwordError; }
    @Nullable
    Integer getConfirmPasswordError() { return confirmPasswordError; }
    boolean isDataValid() { return isDataValid; }
}