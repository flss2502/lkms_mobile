package com.example.lkms.ui.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.lkms.R;
import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private FragmentLoginBinding binding; // Sử dụng ViewBinding
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private NavController navController;
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(requireContext());

        // Cấu hình Google Sign-In (Rất quan trọng)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ file strings.xml
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        // Đăng ký Activity Result Launcher (thay thế cho onActivityResult đã cũ)
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleGoogleSignInResult
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Gán hành động cho tất cả các nút từ layout
        binding.buttonGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        binding.buttonLogin.setOnClickListener(v -> signInWithEmailPassword());
        binding.textSignUp.setOnClickListener(v -> navigateToSignUp());
        binding.textForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    /**
     * Bắt đầu luồng đăng nhập bằng Google
     */
    private void signInWithGoogle() {
        showLoading(true);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    /**
     * Xử lý kết quả trả về từ cửa sổ Google
     */
    private void handleGoogleSignInResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
            try {
                // Đăng nhập Google thành công, giờ xác thực với Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Đăng nhập Google thất bại
                Log.w(TAG, "Google sign in failed", e);
                showLoading(false);
                Toast.makeText(getContext(), "Đăng nhập Google thất bại.", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Người dùng đóng cửa sổ
            showLoading(false);
        }
    }

    /**
     * Xác thực với Firebase dùng token của Google
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập Firebase thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Lưu/Cập nhật người dùng vào CSDL SQLite
                        saveUserToDatabase(user);
                        // Chuyển đến màn hình chính
                        navigateToHome();
                    } else {
                        // Đăng nhập Firebase thất bại
                        showLoading(false);
                        Toast.makeText(getContext(), "Xác thực Firebase thất bại.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Xử lý đăng nhập bằng Email/Password
     */
    private void signInWithEmailPassword() {
        String email = binding.textInputEmail.getEditText().getText().toString().trim();
        String password = binding.textInputPassword.getEditText().getText().toString().trim();

        if (!validateForm(email, password)) {
            return;
        }

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Đăng nhập thành công, kiểm tra xác thực email
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            // (Không cần lưu vào DB, vì họ đã đăng ký)
                            navigateToHome();
                        } else {
                            showLoading(false);
                            Toast.makeText(getContext(), "Email chưa được xác thực. Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
                            if (user != null) {
                                user.sendEmailVerification(); // Gửi lại email
                            }
                            mAuth.signOut(); // Đăng xuất
                        }
                    } else {
                        // Đăng nhập thất bại
                        showLoading(false);
                        Toast.makeText(getContext(), "Đăng nhập thất bại. Kiểm tra lại email/mật khẩu.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Lưu/Cập nhật thông tin người dùng vào CSDL SQLite cục bộ
     */
    private void saveUserToDatabase(FirebaseUser user) {
        if (user == null) return;

        // Kiểm tra xem người dùng đã tồn tại trong DB chưa
        String existingRole = dbHelper.getUserRole(user.getUid());

        // Nếu chưa có (existingRole == null), gán vai trò mặc định
        // (Trong app thực tế, Manager sẽ gán vai trò sau)
        String finalRole = (existingRole != null) ? existingRole : "Researcher";

        dbHelper.insertOrUpdateUser(
                user.getUid(),
                user.getDisplayName(),
                user.getEmail(),
                finalRole
        );
    }

    /**
     * Xử lý Dialog Quên mật khẩu
     */
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Reset Mật khẩu");
        builder.setMessage("Nhập email của bạn để nhận link reset:");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPasswordResetEmail(email);
        });
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        showLoading(true);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Email reset đã được gửi.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi: Không thể gửi email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Điều hướng đến màn hình Đăng ký
     */
    private void navigateToSignUp() {
        try {
            // (Đảm bảo ID "action_loginFragment_to_signUpFragment" tồn tại trong mobile_navigation.xml)
            navController.navigate(R.id.action_loginFragment_to_signUpFragment);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi điều hướng đến Sign Up: ", e);
            Toast.makeText(getContext(), "Không tìm thấy màn hình Đăng ký", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Điều hướng đến màn hình Home (sau khi đăng nhập)
     */
    private void navigateToHome() {
        showLoading(false);
        Toast.makeText(getContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        try {
            // (Đảm bảo ID "action_loginFragment_to_nav_home" tồn tại trong mobile_navigation.xml)
            navController.navigate(R.id.action_loginFragment_to_nav_home);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi điều hướng đến Home: ", e);
            Toast.makeText(getContext(), "Không tìm thấy màn hình Home", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kiểm tra nhanh form email/pass
     */
    private boolean validateForm(String email, String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            binding.textInputEmail.setError("Bắt buộc.");
            valid = false;
        } else {
            binding.textInputEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            binding.textInputPassword.setError("Bắt buộc.");
            valid = false;
        } else {
            binding.textInputPassword.setError(null);
        }
        return valid;
    }

    /**
     * Hiển thị/ẩn ProgressBar
     */
    private void showLoading(boolean isLoading) {
        if (binding == null) return; // (Tránh crash nếu fragment bị hủy)

        binding.loading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!isLoading);
        binding.buttonGoogleLogin.setEnabled(!isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh rò rỉ bộ nhớ
    }
}