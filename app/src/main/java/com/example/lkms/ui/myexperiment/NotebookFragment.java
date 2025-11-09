package com.example.lkms.ui.myexperiment;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Experiment;
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

        // Lấy UID của người dùng (Rất quan trọng)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserUid = user.getUid();
        } else {
            // Người dùng chưa đăng nhập, không thể tiếp tục
            Toast.makeText(getContext(), "Lỗi: Chưa xác thực người dùng.", Toast.LENGTH_LONG).show();
            // (Trong app thực tế, nên điều hướng về màn hình Login)
            currentUserUid = "guest"; // ID tạm
        }

        // 1. Tạo Factory và truyền UID
        ExperimentViewModelFactory factory = new ExperimentViewModelFactory(
                requireActivity().getApplication(),
                currentUserUid
        );

        // 2. Lấy ViewModel bằng Factory (Ràng buộc vào Activity)
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

        setupRecyclerView();
        setupSearch();
        setupFab();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

    private void setupRecyclerView() {
        adapter = new NotebookAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Xử lý Click (để Mở)
        adapter.setOnItemClickListener(experiment -> {
            // ===== BƯỚC TIẾP THEO (TƯƠNG LAI) =====
            // Mở màn hình chi tiết (Note Taking) cho thí nghiệm này
            // Ví dụ:
            // NavHostFragment.findNavController(this)
            //    .navigate(R.id.action_notebookFragment_to_experimentDetailFragment,
            //              bundleOf("EXPERIMENT_ID" to experiment.getId()));
            Toast.makeText(getContext(), "Mở ghi chú cho: " + experiment.getName(), Toast.LENGTH_SHORT).show();
        });

        // Xử lý Long Click (để XÓA)
        adapter.setOnItemLongClickListener(this::showDeleteConfirmationDialog);
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
            // Mở Dialog Thêm Mới
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
