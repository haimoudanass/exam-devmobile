package com.smartcity.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng CASABLANCA_CENTER = new LatLng(33.5731, -7.5898);

    private SessionManager sessionManager;
    private ApiClient apiClient;
    private GoogleMap googleMap;
    private ProgressBar progressBar;
    private final Map<Marker, Report> markerReports = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        apiClient = new ApiClient();
        progressBar = view.findViewById(R.id.mapProgress);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CASABLANCA_CENTER, 12f));
        googleMap.setOnMarkerClickListener(marker -> {
            Report report = markerReports.get(marker);
            if (report != null) {
                openFullDetail(report);
            }
            return true;
        });
        googleMap.setOnInfoWindowClickListener(marker -> {
            Report report = markerReports.get(marker);
            if (report != null) {
                openFullDetail(report);
            }
        });
        loadReports();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleMap != null && sessionManager != null && sessionManager.isLoggedIn()) {
            loadReports();
        }
    }

    private void openFullDetail(Report report) {
        Intent intent = new Intent(requireContext(), ReportDetailActivity.class);
        intent.putExtra(ReportDetailActivity.EXTRA_REPORT, report);
        startActivity(intent);
    }

    private void loadReports() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        apiClient.fetchReports(sessionManager.getToken(), new ApiClient.ApiCallback<List<Report>>() {
            @Override
            public void onSuccess(List<Report> reports) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    renderMarkers(reports);
                });
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            getString(R.string.map_error) + ": " + message,
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void renderMarkers(List<Report> reports) {
        if (googleMap == null) {
            return;
        }
        googleMap.clear();
        markerReports.clear();

        if (reports.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CASABLANCA_CENTER, 12f));
            return;
        }

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        int placed = 0;
        for (Report report : reports) {
            if (!hasValidLocation(report)) {
                continue;
            }
            LatLng pos = new LatLng(report.latitude, report.longitude);
            String snippet = buildMarkerSnippet(report);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(BadgeHelper.formatCategoryLabel(requireContext(), report.category))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            BadgeHelper.markerHue(report.category))));
            markerReports.put(marker, report);
            bounds.include(pos);
            placed++;
        }

        if (placed == 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CASABLANCA_CENTER, 12f));
            return;
        }

        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100));
        } catch (Exception e) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CASABLANCA_CENTER, 12f));
        }
    }

    private static boolean hasValidLocation(Report report) {
        if (report.latitude == 0.0 && report.longitude == 0.0) {
            return false;
        }
        return report.latitude >= -90.0 && report.latitude <= 90.0
                && report.longitude >= -180.0 && report.longitude <= 180.0;
    }

    private String buildMarkerSnippet(Report report) {
        String text = report.description != null && !report.description.isEmpty()
                ? report.description
                : report.title;
        if (text == null) {
            text = "";
        }
        if (text.length() > 80) {
            text = text.substring(0, 77) + "...";
        }
        String status = DisplayLabels.status(requireContext(), report.status);
        if (text.isEmpty()) {
            return status;
        }
        return status.isEmpty() ? text : text + " • " + status;
    }
}
