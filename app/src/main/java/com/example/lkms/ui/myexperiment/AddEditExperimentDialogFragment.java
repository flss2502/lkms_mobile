package com.example.lkms.ui.myexperiment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lkms.R;
import com.example.lkms.data.models.Experiment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddEditExperimentDialogFragment extends DialogFragment {

    private static final String ARG_ITEM = "experiment_item";

    private NotebookViewModel viewModel;
    private Experiment currentExperiment;
    private boolean isEditMode = false;

    // Views
    private TextInputEditText editName, editDueDate;
    private AutoCompleteTextView dropdownStatus;
    private TextInputLayout layoutName;

    public static AddEditExperimentDialogFragment newInstance() {
        return new AddEditExperimentDialogFragment();
    }

    public static AddEditExperimentDialogFragment newInstance(Experiment experiment) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEM, experiment);
        AddEditExperimentDialogFragment fragment = new AddEditExperimentDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy ViewModel từ Activity (để chia sẻ với Fragment)
        viewModel = new ViewModelProvider(requireActivity()).get(NotebookViewModel.class);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM)) {
            currentExperiment = (Experiment) getArguments().getSerializable(ARG_ITEM);
            isEditMode = true;
        } else {
            isEditMode = false;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_edit_experiment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind views
        editName = view.findViewById(R.id.edit_exp_name);
        editDueDate = view.findViewById(R.id.edit_exp_due_date);
        dropdownStatus = view.findViewById(R.id.dropdown_exp_status);
        layoutName = view.findViewById(R.id.layout_exp_name);
        MaterialButton buttonCancel = view.findViewById(R.id.button_cancel);
        MaterialButton buttonSave = view.findViewById(R.id.button_save);

        setupDropdown();

        if (isEditMode) {
            getDialog().setTitle("Chỉnh sửa Thí nghiệm");
            populateFields();
        } else {
            getDialog().setTitle("Tạo Thí nghiệm mới");
        }

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> saveItem());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setupDropdown() {
        String[] statuses = new String[]{"In Progress", "Paused", "Completed", "Pending"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, statuses);
        dropdownStatus.setAdapter(adapter);
    }

    private void populateFields() {
        if (currentExperiment == null) return;
        editName.setText(currentExperiment.getName());
        editDueDate.setText(currentExperiment.getDueDate());
        dropdownStatus.setText(currentExperiment.getStatus(), false); // false để không lọc
    }

    private void saveItem() {
        String name = editName.getText().toString().trim();
        String status = dropdownStatus.getText().toString().trim();
        String dueDate = editDueDate.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            layoutName.setError("Tên không được để trống");
            return;
        }

        if (TextUtils.isEmpty(status)) {
            status = "Pending"; // Mặc định
        }

        if (isEditMode) {
            // Cập nhật
            currentExperiment.setName(name);
            currentExperiment.setStatus(status);
            currentExperiment.setDueDate(dueDate);
            viewModel.update(currentExperiment);
            Toast.makeText(getContext(), "Đã cập nhật", Toast.LENGTH_SHORT).show();
        } else {
            // Thêm mới
            Experiment newExperiment = new Experiment(name, status, dueDate);
            viewModel.insert(newExperiment);
            Toast.makeText(getContext(), "Đã tạo thí nghiệm", Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }
}