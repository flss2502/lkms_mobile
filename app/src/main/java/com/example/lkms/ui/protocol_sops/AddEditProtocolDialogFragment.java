package com.example.lkms.ui.protocol_sops;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lkms.R;
import com.example.lkms.data.models.Protocol;
import com.example.lkms.utils.DriveServiceHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditProtocolDialogFragment extends DialogFragment {

    private static final String TAG = "AddEditProtocolDialog";
    private static final String ARG_ITEM = "protocol_item";

    private ProtocolViewModel viewModel;
    private DriveServiceHelper driveService;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    private Protocol currentProtocol;
    private boolean isEditMode = false;

    // Views
    private TextInputEditText editTitle, editVersion, editProtoLink; // <-- Sửa
    private AutoCompleteTextView dropdownType;
    private Button buttonSelectFile;
    private TextView textFileName, textUploadStatus;
    private ProgressBar progressBarUpload;
    private MaterialButton buttonCancel, buttonSave;
    private TextInputLayout layoutTitle, layoutProtoLink; // <-- Sửa

    // Trạng thái file
    private Uri selectedFileUri;
    private String uploadedFileUrl;
    private String uploadedFileMimeType;
    private boolean isUploading = false;

    // Dùng cho Edit mode
    public static AddEditProtocolDialogFragment newInstance(Protocol protocol) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, protocol);
        AddEditProtocolDialogFragment fragment = new AddEditProtocolDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // Dùng cho Add mode
    public static AddEditProtocolDialogFragment newInstance(DriveServiceHelper driveService, ActivityResultLauncher<Intent> launcher) {
        AddEditProtocolDialogFragment fragment = new AddEditProtocolDialogFragment();
        fragment.driveService = driveService; // Truyền service và launcher
        fragment.filePickerLauncher = launcher;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel từ Activity
        viewModel = new ViewModelProvider(requireActivity()).get(ProtocolViewModel.class);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM)) {
            currentProtocol = (Protocol) getArguments().getSerializable(ARG_ITEM);
            isEditMode = true;
            // Lấy service/launcher từ fragment cha khi ở chế độ Sửa
            if (getParentFragment() instanceof ProtocolFragment) {
                ProtocolFragment parent = (ProtocolFragment) getParentFragment();
                this.driveService = parent.driveService;
                this.filePickerLauncher = parent.filePickerLauncher;
            }
        } else {
            isEditMode = false;
        }

        // Đăng ký lắng nghe kết quả file Uri từ Fragment
        // Chỉ lắng nghe khi ở chế độ Thêm, hoặc Sửa (nếu logic cho phép)
        viewModel.getSelectedFileUri().observe(this, uri -> {
            if (uri != null) {
                selectedFileUri = uri;
                textFileName.setText(getFileNameFromUri(uri));
                textFileName.setVisibility(View.VISIBLE);

                // Xóa link nếu đã nhập
                editProtoLink.setText("");

                // Reset trạng thái upload
                uploadedFileUrl = null;
                uploadedFileMimeType = null;
                viewModel.clearSelectedFileUri(); // Xóa Uri sau khi nhận
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_edit_protocol, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupDropdown();
        setupButtons();

        if (isEditMode) {
            getDialog().setTitle("Chỉnh sửa tài liệu");
            populateFields();
        } else {
            getDialog().setTitle("Thêm tài liệu mới");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Set kích thước dialog
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void bindViews(View view) {
        editTitle = view.findViewById(R.id.edit_proto_title);
        dropdownType = view.findViewById(R.id.dropdown_proto_type);
        editVersion = view.findViewById(R.id.edit_proto_version);

        // Views đã thay đổi
        editProtoLink = view.findViewById(R.id.edit_proto_link);
        layoutProtoLink = view.findViewById(R.id.layout_proto_link);
        buttonSelectFile = view.findViewById(R.id.button_select_file);

        textFileName = view.findViewById(R.id.text_file_name);
        textUploadStatus = view.findViewById(R.id.text_upload_status);
        progressBarUpload = view.findViewById(R.id.progress_bar_upload);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonSave = view.findViewById(R.id.button_save);
        layoutTitle = view.findViewById(R.id.layout_proto_title);
    }

    private void setupDropdown() {
        String[] types = new String[]{"SOP", "PROTOCOL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, types);
        dropdownType.setAdapter(adapter);
    }

    private void setupButtons() {
        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> saveProtocol());

        // Khi nhấn nút "Tải file"
        buttonSelectFile.setOnClickListener(v -> {
            if (isUploading) return;
            openFilePicker();
        });

        // Khi gõ vào ô "Dán link"
        editProtoLink.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !TextUtils.isEmpty(editProtoLink.getText())) {
                // Nếu người dùng gõ vào ô link, xóa file đã chọn (nếu có)
                selectedFileUri = null;
                textFileName.setVisibility(View.GONE);
                textFileName.setText("");
            }
        });
    }

    private void openFilePicker() {
        if (filePickerLauncher == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy trình chọn file", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void populateFields() {
        if (currentProtocol == null) return;
        editTitle.setText(currentProtocol.getTitle());
        dropdownType.setText(currentProtocol.getType(), false);
        editVersion.setText(String.valueOf(currentProtocol.getVersion()));

        if ("HTML".equals(currentProtocol.getContentType())) {
            // Nếu là link, điền vào ô link
            editProtoLink.setText(currentProtocol.getContentData());
            buttonSelectFile.setEnabled(false); // Vô hiệu hóa tải file
        } else {
            // Nếu là file, hiển thị tên và vô hiệu hóa ô link
            textFileName.setText("File đã tải lên: " + getFileNameFromUrl(currentProtocol.getContentData()));
            textFileName.setVisibility(View.VISIBLE);
            editProtoLink.setEnabled(false);
            buttonSelectFile.setText("Thay đổi file (Chưa hỗ trợ)");
            buttonSelectFile.setEnabled(false); // (Logic sửa file phức tạp hơn, tạm thời vô hiệu hóa)
        }
    }

    private void saveProtocol() {
        if (isUploading) {
            Toast.makeText(getContext(), "Đang tải file lên, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = editTitle.getText().toString().trim();
        String type = dropdownType.getText().toString().trim();
        String versionStr = editVersion.getText().toString().trim();
        String linkUrl = editProtoLink.getText().toString().trim();

        // Validate (Kiểm tra)
        if (TextUtils.isEmpty(title)) {
            layoutTitle.setError("Tên không được để trống");
            return;
        } else {
            layoutTitle.setError(null);
        }

        // Validate: Không được vừa chọn file vừa dán link
        if (selectedFileUri != null && !TextUtils.isEmpty(linkUrl)) {
            layoutProtoLink.setError("Chỉ chọn 1 trong 2: Tải file hoặc Dán link");
            Toast.makeText(getContext(), "Bạn vừa chọn file, vui lòng xóa link đã dán.", Toast.LENGTH_LONG).show();
            return;
        }

        // Validate: Phải chọn 1 trong 2 (khi thêm mới)
        if (!isEditMode && selectedFileUri == null && TextUtils.isEmpty(linkUrl)) {
            layoutProtoLink.setError("Vui lòng tải file hoặc dán link");
            return;
        }

        layoutProtoLink.setError(null);

        int version = 1;
        try {
            version = Integer.parseInt(versionStr);
        } catch (NumberFormatException e) {
            version = 1; // Mặc định
        }

        String authorId = GoogleSignIn.getLastSignedInAccount(requireContext()).getId();

        // Xử lý lưu
        if (!TextUtils.isEmpty(linkUrl)) {
            // LƯU DẠNG LINK (HTML/Google Doc)
            // (Fragment sẽ mở link này trong trình duyệt)
            saveToDatabase(title, type, version, authorId, "HTML", linkUrl, "text/html");

        } else {
            // LƯU DẠNG FILE (Tải lên)
            handleSaveFile(title, type, version, authorId);
        }
    }

    private void handleSaveFile(String title, String type, int version, String authorId) {
        // Trường hợp 1: (Edit) Không chọn file mới, dùng file cũ
        if (isEditMode && uploadedFileUrl == null && selectedFileUri == null) {
            saveToDatabase(title, type, version, authorId,
                    currentProtocol.getContentType(),
                    currentProtocol.getContentData(),
                    currentProtocol.getContentMimeType());
            return;
        }

        // Trường hợp 2: Đã tải file lên xong (có URL)
        if (uploadedFileUrl != null) {
            saveToDatabase(title, type, version, authorId, "FILE", uploadedFileUrl, uploadedFileMimeType);
            return;
        }

        // Trường hợp 3: Đã chọn file nhưng CHƯA tải
        if (selectedFileUri != null) {
            uploadFileAndSave(title, type, version, authorId);
            return;
        }

        // (Đã validate ở saveProtocol, không rơi vào đây)
        Toast.makeText(getContext(), "Vui lòng chọn một file để tải lên", Toast.LENGTH_SHORT).show();
    }

    private void uploadFileAndSave(String title, String type, int version, String authorId) {
        isUploading = true;
        setUploadUIState(true);

        String mimeType = requireContext().getContentResolver().getType(selectedFileUri);
        if (mimeType == null) mimeType = "application/octet-stream";

        String finalMimeType = mimeType;

        driveService.uploadFile(requireContext(), selectedFileUri, mimeType, new DriveServiceHelper.UploadCallback() {
            @Override
            public void onSuccess(com.google.api.services.drive.model.File file) {
                isUploading = false;
                setUploadUIState(false);

                // Đã có URL, giờ lưu vào DB
                uploadedFileUrl = file.getWebViewLink();
                uploadedFileMimeType = file.getMimeType();
                saveToDatabase(title, type, version, authorId, "FILE", uploadedFileUrl, uploadedFileMimeType);
            }

            @Override
            public void onError(Exception e) {
                isUploading = false;
                setUploadUIState(false);
                Log.e(TAG, "Tải file thất bại", e);
                Toast.makeText(getContext(), "Tải file thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveToDatabase(String title, String type, int version, String authorId, String contentType, String contentData, String mimeType) {
        if (isEditMode) {
            // Cập nhật
            currentProtocol.setTitle(title);
            currentProtocol.setType(type);
            currentProtocol.setVersion(version);
            currentProtocol.setContentType(contentType);
            currentProtocol.setContentData(contentData);
            currentProtocol.setContentMimeType(mimeType);
            viewModel.update(currentProtocol);
            Toast.makeText(getContext(), "Đã cập nhật " + title, Toast.LENGTH_SHORT).show();
        } else {
            // Thêm mới
            Protocol newProtocol = new Protocol(title, type, version, authorId, contentType, contentData, mimeType);
            viewModel.insert(newProtocol);
            Toast.makeText(getContext(), "Đã thêm " + title, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    private void setUploadUIState(boolean uploading) {
        isUploading = uploading;
        if (uploading) {
            progressBarUpload.setVisibility(View.VISIBLE);
            textUploadStatus.setVisibility(View.VISIBLE);
            buttonSave.setEnabled(false);
            buttonCancel.setEnabled(false);
            buttonSelectFile.setEnabled(false);
            editProtoLink.setEnabled(false);
        } else {
            progressBarUpload.setVisibility(View.GONE);
            textUploadStatus.setVisibility(View.GONE);
            buttonSave.setEnabled(true);
            buttonCancel.setEnabled(true);
            buttonSelectFile.setEnabled(true);
            editProtoLink.setEnabled(true);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "unknown_file";
        try (android.database.Cursor cursor = requireContext().getContentResolver()
                .query(uri, null, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) { Log.e(TAG, "Lỗi khi lấy tên file", e); }
        return fileName;
    }

    private String getFileNameFromUrl(String url) {
        if(url == null) return "N/A";
        try {
            return Uri.parse(url).getLastPathSegment();
        } catch (Exception e) {
            return "link";
        }
    }
}