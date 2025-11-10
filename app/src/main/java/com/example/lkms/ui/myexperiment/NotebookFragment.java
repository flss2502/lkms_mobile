package com.example.lkms.ui.myexperiment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController; // <-- BỔ SUNG IMPORT
import androidx.navigation.Navigation; // <-- BỔ SUNG IMPORT
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Experiment;
import com.example.lkms.ui.myexperiment.ExperimentViewModelFactory;
import com.example.lkms.ui.myexperiment.NotebookAdapter;
import com.example.lkms.ui.myexperiment.NotebookViewModel;
import com.example.lkms.ui.myexperiment.AddEditExperimentDialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class NotebookFragment extends Fragment {

    private NotebookViewModel viewModel;
    private NotebookAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty;
    private SearchView searchView;
    private FloatingActionButton fab;
    private String currentUserUid;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserUid = user.getUid();
        } else {
            Toast.makeText(getContext(), "Lỗi: Chưa xác thực người dùng.", Toast.LENGTH_LONG).show();
            currentUserUid = "guest";
        }

        ExperimentViewModelFactory factory = new ExperimentViewModelFactory(
                requireActivity().getApplication(),
                currentUserUid
        );
        viewModel = new ViewModelProvider(requireActivity(), factory).get(NotebookViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notebook, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_notebook);
        textEmpty = root.findViewById(R.id.text_empty_notebook);
        searchView = root.findViewById(R.id.search_view_notebook);
        fab = root.findViewById(R.id.fab_add_experiment);

        // (Chúng ta sẽ gọi setupRecyclerView trong onViewCreated)

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gọi setup ở đây (sau khi View đã được tạo) sẽ an toàn hơn
        setupRecyclerView(view);
        setupSearch();
        setupFab();

        // 3. Quan sát dữ liệu
        viewModel.getFilteredExperiments().observe(getViewLifecycleOwner(), experiments -> {
            adapter.submitList(experiments);
            if (experiments == null || experiments.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Sửa: Hàm này giờ nhận View
     */
    private void setupRecyclerView(@NonNull View view) {
        adapter = new NotebookAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(experiment -> {
            Log.d("NotebookFragment", "Đã nhấp vào Experiment ID: " + experiment.getId());
            Bundle bundle = new Bundle();
            bundle.putInt("EXPERIMENT_ID", experiment.getId());
            bundle.putString("EXPERIMENT_NAME", experiment.getName());

            try {
                // ===== SỬA LỖI Ở ĐÂY =====
                // Dùng Navigation.findNavController(view) thay vì NavHostFragment
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_notebookFragment_to_labNoteFragment, bundle);
                // ========================

            } catch (Exception e) {
                Log.e("NotebookFragment", "Lỗi điều hướng: " + e.getMessage());
                Toast.makeText(getContext(), "Lỗi điều hướng (Action not found)", Toast.LENGTH_SHORT).show();
            }
        });

        adapter.setOnItemLongClickListener(experiment -> {
            // Hiển thị menu lựa chọn
            showEditOrDeleteDialog(experiment);
        });
    }

    private void showEditOrDeleteDialog(Experiment experiment) {
        final CharSequence[] options = {"Sửa thông tin", "Xóa thí nghiệm", "Hủy"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tùy chọn: " + experiment.getName());

        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Sửa thông tin")) {
                // ===== HÀNH ĐỘNG "SỬA" (EDIT) =====
                // Mở Dialog Add/Edit ở chế độ Sửa
                AddEditExperimentDialogFragment editDialog = AddEditExperimentDialogFragment.newInstance(experiment);
                editDialog.show(getParentFragmentManager(), "EditExperimentDialog");

            } else if (options[item].equals("Xóa thí nghiệm")) {
                // ===== HÀNH ĐỘNG "XÓA" (DELETE) =====
                showDeleteConfirmationDialog(experiment); // Gọi dialog xác nhận xóa

            } else if (options[item].equals("Hủy")) {
                dialog.dismiss();
            }
        });
        builder.show();
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

    private void setupFab() {
        fab.setOnClickListener(v -> {
            AddEditExperimentDialogFragment dialog = AddEditExperimentDialogFragment.newInstance();
            dialog.show(getParentFragmentManager(), "AddExperimentDialog");
        });
    }

    private void showDeleteConfirmationDialog(Experiment experiment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thí nghiệm này?\n(" + experiment.getName() + ")\n\nToàn bộ ghi chú (notes) liên quan cũng sẽ bị xóa vĩnh viễn.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.delete(experiment);
                    Toast.makeText(getContext(), "Đã xóa " + experiment.getName(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_dialog_alert)
                .show();
    }
}