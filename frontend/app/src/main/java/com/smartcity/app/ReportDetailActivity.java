package com.smartcity.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.appbar.MaterialToolbar;

public class ReportDetailActivity extends AppCompatActivity {

    private static final String TAG = "ReportDetail";

    public static final String EXTRA_REPORT = "extra_report";

    private Report report;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView distanceView;
    private Button navigateButton;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fine = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if (Boolean.TRUE.equals(fine)) {
                    fetchUserLocationAndDistance();
                } else {
                    showLocationDenied();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        report = (Report) getIntent().getSerializableExtra(EXTRA_REPORT);
        if (report == null) {
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.detail_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        distanceView = findViewById(R.id.detailDistance);
        navigateButton = findViewById(R.id.btnNavigate);

        ReportUiHelper.bindDetail(
                this,
                report,
                findViewById(R.id.detailImage),
                findViewById(R.id.detailTitle),
                findViewById(R.id.detailCategory),
                findViewById(R.id.detailStatus),
                findViewById(R.id.detailDescription),
                findViewById(R.id.detailCoords)
        );

        navigateButton.setOnClickListener(v -> openGoogleMapsNavigation());
        ensureLocationPermissionAndFetchDistance();
    }

    private void ensureLocationPermissionAndFetchDistance() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocationAndDistance();
            return;
        }
        distanceView.setText(R.string.detail_distance_permission);
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void fetchUserLocationAndDistance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            showLocationDenied();
            return;
        }

        distanceView.setText(R.string.detail_distance_loading);

        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this::onUserLocationReady)
                .addOnFailureListener(e -> {
                    Log.w(TAG, "getCurrentLocation failed", e);
                    showLocationError();
                });
    }

    private void onUserLocationReady(Location userLocation) {
        if (userLocation == null) {
            Log.w(TAG, "getCurrentLocation returned null (GPS off or no fix)");
            showLocationError();
            return;
        }

        Location reportLocation = new Location("report");
        reportLocation.setLatitude(report.latitude);
        reportLocation.setLongitude(report.longitude);

        float meters = userLocation.distanceTo(reportLocation);
        distanceView.setText(formatDistanceText(meters));
    }

    private String formatDistanceText(float meters) {
        if (meters < 1000f) {
            return getString(R.string.detail_distance_meters, Math.round(meters));
        }
        return getString(R.string.detail_distance_kilometers, meters / 1000.0);
    }

    private void showLocationError() {
        distanceView.setText(R.string.detail_distance_error);
    }

    private void showLocationDenied() {
        distanceView.setText(R.string.detail_distance_permission);
        Log.w(TAG, "ACCESS_FINE_LOCATION denied");
    }

    private void openGoogleMapsNavigation() {
        String uri = "google.navigation:q=" + report.latitude + "," + report.longitude;
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            return;
        }

        Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        if (fallback.resolveActivity(getPackageManager()) != null) {
            startActivity(fallback);
            return;
        }

        Toast.makeText(this, R.string.detail_navigate_error, Toast.LENGTH_LONG).show();
        Log.w(TAG, "No app can handle navigation URI: " + uri);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
