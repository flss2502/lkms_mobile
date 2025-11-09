package com.example.lkms.ui.inventory;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lkms.R;
import com.example.lkms.data.models.InventoryItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditInventoryDialogFragment extends DialogFragment {

    private static final String ARG_ITEM = "inventory_item";

    private InventoryViewModel inventoryViewModel;
    private InventoryItem currentItem; // Item đang chỉnh sửa
    private boolean isEditMode = false;

    // Views
    private TextInputEditText editName, editCategory, editQuantity, editUnit, editLocation, editDescription;
    private TextInputLayout layoutName, layoutQuantity;
    private MaterialButton buttonCancel, buttonSave;

    /**
     * Tạo instance cho CHẾ ĐỘ SỬA (EDIT)
     */
    public static AddEditInventoryDialogFragment newInstance(InventoryItem item) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, item); // Gửi item cần sửa vào
        AddEditInventoryDialogFragment fragment = new AddEditInventoryDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Tạo instance cho CHẾ ĐỘ THÊM (ADD)
     */
    public static AddEditInventoryDialogFragment newInstance() {
        return new AddEditInventoryDialogFragment(); // Không cần tham số
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy Shared ViewModel từ InventoryFragment (parent)
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);

        // Kiểm tra xem đây là Sửa hay Thêm
        if (getArguments() != null && getArguments().containsKey(ARG_ITEM)) {
            currentItem = (InventoryItem) getArguments().getSerializable(ARG_ITEM);
            isEditMode = true;
        } else {
            isEditMode = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_edit_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các views
        editName = view.findViewById(R.id.edit_name);
        editCategory = view.findViewById(R.id.edit_category);
        editQuantity = view.findViewById(R.id.edit_quantity);
        editUnit = view.findViewById(R.id.edit_unit);
        editLocation = view.findViewById(R.id.edit_location);
        editDescription = view.findViewById(R.id.edit_description);

        layoutName = view.findViewById(R.id.layout_name);
        layoutQuantity = view.findViewById(R.id.layout_quantity);

        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonSave = view.findViewById(R.id.button_save);

        // Nếu là CHẾ ĐỘ SỬA, điền dữ liệu cũ vào form
        if (isEditMode) {
            getDialog().setTitle("Chỉnh sửa vật tư");
            populateFields();
        } else {
            getDialog().setTitle("Thêm vật tư mới");
        }

        // Xử lý nút Hủy
        buttonCancel.setOnClickListener(v -> dismiss());

        // Xử lý nút Lưu
        buttonSave.setOnClickListener(v -> saveItem());
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

    private void populateFields() {
        if (currentItem == null) return;
        editName.setText(currentItem.getName());
        editCategory.setText(currentItem.getCategory());
        editQuantity.setText(String.valueOf(currentItem.getQuantity()));
        editUnit.setText(currentItem.getUnit());
        editLocation.setText(currentItem.getLocation());
        editDescription.setText(currentItem.getDescription());
    }

    private void saveItem() {
        // Lấy dữ liệu từ form
        String name = editName.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String quantityStr = editQuantity.getText().toString().trim();
        String unit = editUnit.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        String description = editDescription.getText().toString().trim();

        // Validate (Kiểm tra)
        if (TextUtils.isEmpty(name)) {
            layoutName.setError("Tên không được để trống");
            return;
        } else {
            layoutName.setError(null);
        }

        double quantity = 0.0;
        if (TextUtils.isEmpty(quantityStr)) {
            layoutQuantity.setError("Số lượng không được để trống");
            return;
        } else {
            try {
                quantity = Double.parseDouble(quantityStr);
                layoutQuantity.setError(null);
            } catch (NumberFormatException e) {
                layoutQuantity.setError("Số lượng không hợp lệ");
                return;
            }
        }

        // Thực hiện lưu
        if (isEditMode) {
            // Cập nhật item hiện tại
            currentItem.setName(name);
            currentItem.setCategory(category);
            currentItem.setQuantity(quantity);
            currentItem.setUnit(unit);
            currentItem.setLocation(location);
            currentItem.setDescription(description);

            inventoryViewModel.update(currentItem); // Gọi ViewModel
            Toast.makeText(getContext(), "Đã cập nhật " + name, Toast.LENGTH_SHORT).show();

        } else {
            // Tạo item mới
            InventoryItem newItem = new InventoryItem(name, description, quantity, unit, location, category);

            inventoryViewModel.insert(newItem); // Gọi ViewModel
            Toast.makeText(getContext(), "Đã thêm " + name, Toast.LENGTH_SHORT).show();
        }

        dismiss(); // Đóng dialog
    }
}