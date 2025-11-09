package com.example.lkms.ui.labnote;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.databinding.FragmentLabNoteBinding;
import com.example.lkms.ui.labnote.LabNoteAdapter;

import jp.wasabeef.richeditor.RichEditor;

public class LabNoteFragment extends Fragment {

    private FragmentLabNoteBinding binding; // Sử dụng ViewBinding
    private RichEditor editor; // (Không dùng 'm' Editor)

    private LabNoteViewModel viewModel;
    private LabNoteAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textExperimentTitle;

    private int currentExperimentId = -1;
    private String currentExperimentName = "";

    private static final int REQUEST_IMAGE_PICK = 1001;

    // (Hàm này dùng để nhận ID và Tên từ NotebookFragment)
    // (Lưu ý: Hàm newInstance() KHÔNG nên có trong file, vì nó không phải static)
    // Bạn nên gọi nó từ NotebookFragment
    // static LabNoteFragment newInstance(int experimentId, String experimentName) { ... }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy ID và Tên thí nghiệm
        if (getArguments() != null) {
            currentExperimentId = getArguments().getInt("EXPERIMENT_ID", -1);
            currentExperimentName = getArguments().getString("EXPERIMENT_NAME", "Chi tiết thí nghiệm");
        }

        if (currentExperimentId == -1) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID thí nghiệm.", Toast.LENGTH_LONG).show();
            // (Nên đóng Fragment ở đây)
            getParentFragmentManager().popBackStack(); // Quay lại
            return;
        }

        // ===== SỬA LỖI: Lấy ViewModel bằng Factory (để truyền ID vào) =====
        LabNoteViewModel.LabNoteViewModelFactory factory =
                new LabNoteViewModel.LabNoteViewModelFactory(requireActivity().getApplication(), currentExperimentId);
        viewModel = new ViewModelProvider(this, factory).get(LabNoteViewModel.class);
        // ===============================================================
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Khởi tạo ViewBinding
        binding = FragmentLabNoteBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo view bằng binding (Đảm bảo ID trong XML khớp)
        editor = binding.editor;
        recyclerView = binding.recyclerViewLabNotes;
        textExperimentTitle = binding.textExperimentTitle;

        textExperimentTitle.setText("Sổ tay: " + currentExperimentName);

        // Gọi các hàm setup
        setupRecyclerView();
        setupEditor();

        // Quan sát (Observe) danh sách ghi chú
        viewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            adapter.submitList(notes);
            // (Tùy chọn) Tự động cuộn xuống cuối
            if (notes.size() > 0) {
                recyclerView.post(() -> recyclerView.smoothScrollToPosition(notes.size() - 1));
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new LabNoteAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupEditor() {
        editor.setEditorHeight(200);
        editor.setEditorFontSize(18);
        editor.setEditorFontColor(Color.BLACK);
        editor.setPadding(10, 10, 10, 10);
        editor.setPlaceholder("Nhập ghi chú mới tại đây...");

        // Gán chức năng cho các nút Toolbar (dùng binding)
        binding.btnUndo.setOnClickListener(v -> editor.undo());
        binding.btnRedo.setOnClickListener(v -> editor.redo());
        binding.btnBold.setOnClickListener(v -> editor.setBold());
        binding.btnItalic.setOnClickListener(v -> editor.setItalic());
        binding.btnUnderline.setOnClickListener(v -> editor.setUnderline());
        binding.btnHeading.setOnClickListener(v -> editor.setHeading(2)); // H2
        binding.btnBullets.setOnClickListener(v -> editor.setBullets());
        binding.btnLink.setOnClickListener(v -> showInsertLinkDialog());
        binding.btnImage.setOnClickListener(v -> openImagePicker());

        // Nút lưu (btnSave) sẽ gọi hàm saveNote()
        binding.btnSave.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String htmlContent = editor.getHtml();

        if (htmlContent == null || TextUtils.isEmpty(htmlContent)) {
            Toast.makeText(getContext(), "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi ViewModel để lưu
        viewModel.saveNote(htmlContent);

        Toast.makeText(getContext(), "Đã lưu ghi chú!", Toast.LENGTH_SHORT).show();

        // Xóa nội dung editor sau khi lưu
        editor.setHtml("");
    }

    private void showInsertLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chèn liên kết");

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_insert_link, null); // (Cần tạo layout này)

        EditText inputUrl = dialogView.findViewById(R.id.inputUrl);
        EditText inputText = dialogView.findViewById(R.id.inputText);

        inputUrl.setHint("Nhập URL (https://...)");
        inputText.setHint("Văn bản hiển thị");

        builder.setView(dialogView);
        builder.setPositiveButton("Chèn", (dialog, which) -> {
            String url = inputUrl.getText().toString().trim();
            String text = inputText.getText().toString().trim();

            if (!url.isEmpty() && !text.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                editor.insertLink(url, text);
            } else {
                Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // 🖼️ Hàm chọn ảnh từ thư viện
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    // 📥 Nhận kết quả chọn ảnh
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                // TODO: Tải ảnh lên Google Drive/Firebase Storage, sau đó
                // editor.insertImage("URL_tra_ve_tu_server", "Hinh_anh");

                // Tạm thời chèn (có thể không hiển thị đúng)
                editor.insertImage(selectedImage.toString(), "Hình ảnh", 320);
                Toast.makeText(getContext(), "Đã chèn ảnh (tạm thời)", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}