package com.example.lkms.ui.account;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.lkms.R;
import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.databinding.FragmentAccountBinding;
import com.example.lkms.databinding.FragmentLoginBinding;
import com.example.lkms.ui.login.LoginFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private FirebaseUser currentUser;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo các dịch vụ
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(requireContext());
        currentUser = mAuth.getCurrentUser();

        // Cấu hình GoogleSignInClient (cần thiết cho việc đăng xuất)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Lấy từ file strings.xml
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadUserProfile();
        setupClickListeners();
    }

    private void loadUserProfile() {
        if (currentUser == null) {
            // Xử lý trường hợp người dùng không (hoặc chưa) đăng nhập
            binding.textAccountName.setText("Khách");
            binding.textAccountEmail.setText("Vui lòng đăng nhập");
            binding.textAccountRole.setText("Vai trò: N/A");
            return;
        }

        // Lấy thông tin từ Firebase/Google
        String name = currentUser.getDisplayName();
        String email = currentUser.getEmail();
        Uri photoUrl = currentUser.getPhotoUrl();

        // Lấy thông tin (Role) từ CSDL SQLite
        String role = dbHelper.getUserRole(currentUser.getUid());

        // Cập nhật UI
        binding.textAccountName.setText(name);
        binding.textAccountEmail.setText(email);
        binding.textAccountRole.setText("Vai trò: " + (role != null ? role : "N/A"));

        // Tải ảnh đại diện bằng Glide
        // (Hãy chắc chắn bạn đã thêm 'implementation "com.github.bumptech.glide:glide:4.16.0"' vào build.gradle)
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.ic_account_circle) // Icon mặc định (bạn đã có)
                .circleCrop() // Biến ảnh thành hình tròn
                .into(binding.imageAccountAvatar);
    }

    private void setupClickListeners() {
        // 1. Nút "Quản lý tài khoản Google"
        binding.buttonManageGoogle.setOnClickListener(v -> {
            // Mở trình duyệt đến trang quản lý tài khoản của Google
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://myaccount.google.com"));
            startActivity(intent);
        });

        // 2. Nút "Xóa tài khoản khỏi ứng dụng"
        binding.buttonDeleteAccount.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận Xóa")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản của mình khỏi ứng dụng này?\n\nToàn bộ dữ liệu (thí nghiệm, ghi chú, v.v.) của bạn sẽ bị xóa vĩnh viễn. Hành động này không thể hoàn tác.")
                .setIcon(R.drawable.ic_dialog_alert) // (Icon bạn đã có)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Thực hiện xóa và đăng xuất
                    deleteAccountAndLogout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteAccountAndLogout() {
        if (currentUser == null) return;

        // 1. Xóa dữ liệu trong CSDL SQLite (trên luồng nền)
        // (Bạn cần thêm hàm deleteUserData vào DatabaseHelper)
        new Thread(() -> {
            dbHelper.deleteUserData(currentUser.getUid());
        }).start();

        // 2. Đăng xuất khỏi Firebase
        mAuth.signOut();

        // 3. Đăng xuất khỏi Google
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Toast.makeText(getContext(), "Đã xóa tài khoản và đăng xuất", Toast.LENGTH_SHORT).show();

            // 4. Quay về màn hình Login
            Intent intent = new Intent(getContext(), FragmentLoginBinding.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh rò rỉ bộ nhớ
    }
}