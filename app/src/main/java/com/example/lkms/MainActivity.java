package com.example.lkms;

import android.app.AlertDialog;
import android.content.Intent;
// import android.content.SharedPreferences; // (Không dùng)
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
// import android.widget.ImageButton; // (Không dùng)
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.splashscreen.SplashScreen;
import com.bumptech.glide.Glide;
import com.example.lkms.data.DatabaseHelper; // <-- BỔ SUNG
// import com.example.lkms.ui.login.LoginFragment; // (Không dùng)
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
// import com.google.android.material.snackbar.Snackbar; // (Không dùng)
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull; // (Import thiếu)
// import androidx.activity.OnBackPressedCallback; // (Không dùng)
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lkms.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private DrawerLayout drawer;
    private GoogleSignInClient mGoogleSignInClient;
    private NavController navController;

    // ===== BỔ SUNG: Khai báo Listener và DB Helper =====
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseHelper dbHelper;
    // =================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // ===== BỔ SUNG: Khởi tạo dbHelper =====
        dbHelper = new DatabaseHelper(this);

        // Khởi tạo GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // (Xóa 'FirebaseUser user = mAuth.getCurrentUser();' ở đây, Listener sẽ xử lý)

        MaterialToolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);

        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_protocol, R.id.nav_notebook, R.id.nav_inventory, R.id.nav_booking, R.id.nav_myaccount) // Thêm nav_myaccount
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        // ===== BỔ SUNG: Logic cho 'nav_myaccount' và 'nav_logout' =====
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            drawer.closeDrawer(GravityCompat.START);

            if (id == R.id.nav_logout) {
                showLogoutConfirmationDialog();
                return true;
            } else if (id == R.id.nav_myaccount) {
                navController.navigate(R.id.nav_myaccount);
                return true;
            }

            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        // ===== BỔ SUNG: Logic Ẩn/Hiện Toolbar (Đã sửa) =====
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();

            if (mAppBarConfiguration.getTopLevelDestinations().contains(destId)) {
                // Đây là 5 màn hình gốc (Home, Inventory...)
                toolbar.setVisibility(View.VISIBLE);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            } else {
                // Đây LÀ màn hình con (Account, Login, LabNote...)
                if (destId == R.id.loginFragment || destId == R.id.signUpFragment) {
                    toolbar.setVisibility(View.GONE); // Ẩn toolbar
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                } else {
                    // (Ví dụ: AccountFragment, LabNoteFragment)
                    toolbar.setVisibility(View.VISIBLE); // Hiển thị (sẽ có nút Back <-)
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
            }
        });

        // ===== BỔ SUNG: Khởi tạo AuthStateListener =====
        setupAuthStateListener();
    }

    /**
     * BỔ SUNG: Hàm này sẽ được gọi BẤT CỨ KHI NÀO người dùng Đăng nhập hoặc Đăng xuất
     */
    private void setupAuthStateListener() {
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            updateNavHeader(user); // <-- Gọi hàm cập nhật Header
        };
    }

    /**
     * BỔ SUNG: Tách logic cập nhật Header ra hàm riêng
     */
    private void updateNavHeader(FirebaseUser user) {
        View headerView = binding.navView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tvName);
        TextView tvEmail = headerView.findViewById(R.id.tvEmail);
        ImageView imageView = headerView.findViewById(R.id.imageView);

        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl(); // (Sẽ null nếu là Email/Pass)

            // Lấy Tên (Name) từ CSDL SQLite
            String name = dbHelper.getUserName(uid);

            // Ưu tiên Tên từ CSDL
            if (name == null || name.isEmpty()) {
                tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
            } else {
                tvName.setText(name);
            }

            tvEmail.setText(email);

            // Tải ảnh (Glide tự xử lý nếu photoUrl là null)
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.default_avatar)
                    .circleCrop()
                    .into(imageView);

        } else {
            // Nếu không có user (đã đăng xuất)
            tvName.setText("Đăng nhập để xem thông tin");
            tvEmail.setText("");
            imageView.setImageResource(R.drawable.default_avatar);
        }
    }


    // ===== BỔ SUNG: Gắn/Gỡ Listener theo vòng đời Activity =====
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener); // Bắt đầu lắng nghe
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener); // Dừng lắng nghe
        }
    }
    // ========================================================


    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setIcon(R.drawable.logout)
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    logoutUser();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logoutUser() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            // Điều hướng về Login
            navController.navigate(R.id.loginFragment);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}