package com.example.lkms.ui.protocol_sops;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R; // Bạn sẽ cần tạo layout "fragment_protocol.xml"
import com.example.lkms.utils.GoogleSignInHelper;
import com.example.lkms.data.models.Protocol;
import com.example.lkms.utils.DriveServiceHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class ProtocolFragment extends Fragment {

    private static final String TAG = "ProtocolFragment";

    private ProtocolViewModel viewModel;
    private ProtocolAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty;
    private SearchView searchView;
    private TabLayout tabLayout;

    DriveServiceHelper driveService;
    ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Lấy ViewModel (liên kết với Activity)
        // Điều này cho phép Dialog và Fragment chia sẻ cùng một ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(ProtocolViewModel.class);

        // 2. Khởi tạo DriveServiceHelper
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
        if (account != null && GoogleSignInHelper.hasDriveScope(account)) {
            driveService = new DriveServiceHelper(requireContext(), account);
        } else {
            // Yêu cầu quyền nếu chưa có (ví dụ khi app vừa khởi động)
            Log.w(TAG, "Chưa có quyền Google Drive, đang yêu cầu...");
            GoogleSignInHelper.requestDriveScope(this);
        }

        // 3. Chuẩn bị trình chọn file (sẽ được dùng bởi Dialog)
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Chuyển kết quả này cho Dialog đang lắng nghe
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) {
                            // Gửi Uri này tới Dialog thông qua ViewModel
                            viewModel.setSelectedFileUri(fileUri);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Bạn cần tạo layout "fragment_protocol.xml" (đã cung cấp ở lần trước)
        View root = inflater.inflate(R.layout.fragment_protocol, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_protocol);
        textEmpty = root.findViewById(R.id.text_empty_protocol);
        searchView = root.findViewById(R.id.search_view_protocol);
        tabLayout = root.findViewById(R.id.tab_layout_protocol);
        FloatingActionButton fab = root.findViewById(R.id.fab_add_protocol);

        setupRecyclerView();
        setupTabs();
        setupSearch();
        setupFab(fab);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Quan sát dữ liệu
        viewModel.getFilteredProtocols().observe(getViewLifecycleOwner(), protocols -> {
            if (protocols == null) return;
            adapter.submitList(protocols);
            if (protocols.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            }
        });

        // Tải dữ liệu ban đầu cho tab đầu tiên ("SOP")
        // (Chúng ta giả định tab 0 là SOP)
        viewModel.loadProtocolsByType("SOP");
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    viewModel.loadProtocolsByType("SOP");
                } else {
                    viewModel.loadProtocolsByType("PROTOCOL");
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void setupFab(FloatingActionButton fab) {
        fab.setOnClickListener(v -> {
            // Kiểm tra quyền Drive trước khi mở Dialog
            if (driveService == null) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(requireContext());
                if (account != null && GoogleSignInHelper.hasDriveScope(account)) {
                    driveService = new DriveServiceHelper(requireContext(), account);
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền Google Drive", Toast.LENGTH_SHORT).show();
                    GoogleSignInHelper.requestDriveScope(this); // Yêu cầu lại quyền
                    return;
                }
            }

            // Mở Dialog Thêm Mới
            AddEditProtocolDialogFragment dialog = AddEditProtocolDialogFragment.newInstance(driveService, filePickerLauncher);
            dialog.show(getParentFragmentManager(), "AddProtocolDialog");
        });
    }

    private void setupRecyclerView() {
        adapter = new ProtocolAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Khi nhấn vào item (để XEM)
        adapter.setOnItemClickListener(protocol -> {
            if ("FILE".equals(protocol.getContentType())) {
                // Mở file (PDF/Word) bằng ứng dụng bên ngoài
                openFileWithIntent(protocol.getContentData(), protocol.getContentMimeType());
            } else {
                // Mở file (HTML/Link) bằng trình duyệt
                openLinkInBrowser(protocol.getContentData());
            }
        });

        // Khi nhấn giữ (để XÓA)
        adapter.setOnItemLongClickListener(this::showDeleteConfirmationDialog);
    }

    private void showDeleteConfirmationDialog(Protocol protocol) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa tài liệu này?\n(" + protocol.getTitle() + ")\n\nLưu ý: File trên Google Drive (nếu có) sẽ KHÔNG bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(protocol);
                    Toast.makeText(getContext(), "Đã xóa " + protocol.getTitle(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_dialog_alert) // (Icon bạn đã tạo cho Inventory)
                .show();
    }

    /**
     * Mở file bằng Intent (như đã thảo luận)
     */
    private void openFileWithIntent(String fileUrl, String mimeType) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy URL của file.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Thử mở bằng app chuyên dụng (Drive, Adobe Reader, Office)
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(fileUrl), mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // KHÔNG CÓ APP: Mở bằng trình duyệt (fallback)
            Toast.makeText(getContext(), "Không tìm thấy app. Đang mở bằng trình duyệt...", Toast.LENGTH_SHORT).show();
            openLinkInBrowser(fileUrl);
        }
    }

    private void openLinkInBrowser(String url) {
        if (url == null || url.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy đường dẫn.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Không thể mở đường dẫn.", Toast.LENGTH_SHORT).show();
        }
    }
}