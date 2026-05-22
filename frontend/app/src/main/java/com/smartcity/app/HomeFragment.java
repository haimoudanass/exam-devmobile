package com.smartcity.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class HomeFragment extends Fragment {

    private SessionManager sessionManager;
    private ApiClient apiClient;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private ReportListAdapter adapter;
    private String statusFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        apiClient = new ApiClient();

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        emptyView = view.findViewById(R.id.textEmpty);
        RecyclerView recycler = view.findViewById(R.id.recyclerReports);
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupStatus);
        FloatingActionButton fabVoice = view.findViewById(R.id.fabVoiceAssistant);

        adapter = new ReportListAdapter(report -> {
            Intent intent = new Intent(requireContext(), ReportDetailActivity.class);
            intent.putExtra(ReportDetailActivity.EXTRA_REPORT, report);
            startActivity(intent);
        });

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);

        swipeRefresh.setColorSchemeResources(R.color.accent, R.color.teal_200);
        swipeRefresh.setOnRefreshListener(this::loadReports);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chipPending) {
                statusFilter = "pending";
            } else if (id == R.id.chipResolved) {
                statusFilter = "resolved";
            } else {
                statusFilter = null;
            }
            loadReports();
        });

        fabVoice.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ChatAssistantActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadReports();
    }

    public void loadReports() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        swipeRefresh.setRefreshing(true);
        apiClient.fetchReports(sessionManager.getToken(), statusFilter, new ApiClient.ApiCallback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> reports) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    adapter.setItems(reports);
                    emptyView.setVisibility(reports.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
