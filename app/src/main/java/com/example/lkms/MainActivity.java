package com.example.lkms;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.splashscreen.SplashScreen;
import com.bumptech.glide.Glide;
import com.example.lkms.ui.login.LoginFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.OnBackPressedCallback;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen.installSplashScreen(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        // Khởi tạo GoogleSignInClient
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseUser user = mAuth.getCurrentUser();

        MaterialToolbar toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);

        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_protocol, R.id.nav_notebook, R.id.nav_inventory, R.id.nav_booking)
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                drawer.closeDrawer(GravityCompat.START);
                showLogoutConfirmationDialog();
                return true; // Đã xử lý
            }
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawer(GravityCompat.START);
            }

            return handled;
        });

        // Khi bấm icon menu trên Toolbar
        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        View headerView = navigationView.getHeaderView(0);
        TextView tvName = headerView.findViewById(R.id.tvName);
        TextView tvEmail = headerView.findViewById(R.id.tvEmail);
        ImageView imageView = headerView.findViewById(R.id.imageView);

        if (user != null) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Người dùng");
            tvEmail.setText(user.getEmail());
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.default_avatar)
                        .circleCrop() // (Nên thêm cái này để ảnh tròn)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.default_avatar);
            }
        } else {
            // (Code cũ của bạn)
            tvName.setText("Đăng nhập để xem thông tin");
            tvEmail.setText("");
            imageView.setImageResource(R.drawable.default_avatar);
        }


        // 🟢 Ẩn AppBar và khóa Drawer ở màn hình đăng nhập
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int destId = destination.getId();
            if (destId == R.id.loginFragment || destId == R.id.signUpFragment) {
                toolbar.setVisibility(View.GONE);
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            } else {
                toolbar.setVisibility(View.VISIBLE);
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setIcon(R.drawable.logout) // (Icon bạn đã có)
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    logoutUser();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logoutUser() {
        // 1. Đăng xuất khỏi Firebase
        mAuth.signOut();

        // 2. Đăng xuất khỏi Google
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Toast.makeText(MainActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

            // 3. Chuyển về màn hình Login
            // (Chúng ta điều hướng bằng NavController thay vì Intent để ở trong cùng 1 Activity)
            navController.navigate(R.id.loginFragment);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
