package com.smartcity.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ReportActivity extends AppCompatActivity {

    private static final int PERM_REQUEST = 2001;

    private SessionManager sessionManager;
    private ApiClient apiClient;
    private FusedLocationProviderClient locationClient;

    private EditText titleInput;
    private EditText descriptionInput;
    private TextView locationText;
    private ImageView imagePreview;
    private Button submitButton;

    private Double latitude;
    private Double longitude;
    private File photoFile;

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (Boolean.TRUE.equals(success) && photoFile != null && photoFile.exists()) {
                    imagePreview.setVisibility(View.VISIBLE);
                    imagePreview.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        sessionManager = new SessionManager(this);
        apiClient = new ApiClient();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        titleInput = findViewById(R.id.inputTitle);
        descriptionInput = findViewById(R.id.inputDescription);
        locationText = findViewById(R.id.textLocation);
        imagePreview = findViewById(R.id.imagePreview);
        submitButton = findViewById(R.id.btnSubmitReport);
        Button takePhotoButton = findViewById(R.id.btnTakePhoto);

        takePhotoButton.setOnClickListener(v -> openCamera());
        submitButton.setOnClickListener(v -> submitReport());

        ensurePermissionsAndLoadLocation();
    }

    private void ensurePermissionsAndLoadLocation() {
        String[] needed = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
        };

        boolean missing = false;
        for (String perm : needed) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                missing = true;
                break;
            }
        }

        if (missing) {
            ActivityCompat.requestPermissions(this, needed, PERM_REQUEST);
        } else {
            fetchLocation();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationText.setText(R.string.report_location_error);
            return;
        }

        locationText.setText(R.string.report_location_loading);

        CancellationTokenSource cts = new CancellationTokenSource();
        locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this::onLocationReady)
                .addOnFailureListener(e -> locationText.setText(R.string.report_location_error));
    }

    private void onLocationReady(Location location) {
        if (location == null) {
            locationText.setText(R.string.report_location_error);
            return;
        }
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationText.setText(getString(R.string.report_location_ready, latitude, longitude));
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERM_REQUEST);
            return;
        }

        try {
            photoFile = File.createTempFile("report_", ".jpg", getCacheDir());
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(uri);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitReport() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.report_error_title, Toast.LENGTH_SHORT).show();
            return;
        }

        if (latitude == null || longitude == null) {
            Toast.makeText(this, R.string.report_location_error, Toast.LENGTH_SHORT).show();
            fetchLocation();
            return;
        }

        submitButton.setEnabled(false);
        Toast.makeText(this, R.string.report_submitting, Toast.LENGTH_SHORT).show();

        apiClient.createReport(
                sessionManager.getToken(),
                title,
                description,
                latitude,
                longitude,
                photoFile,
                new ApiClient.ApiCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        runOnUiThread(() -> {
                            submitButton.setEnabled(true);
                            try {
                                JSONObject ai = result.optJSONObject("ai_classification");
                                String category = ai != null ? ai.optString("category", "general") : "general";
                                String categoryLabel = DisplayLabels.category(ReportActivity.this, category);
                                Toast.makeText(
                                        ReportActivity.this,
                                        getString(R.string.report_ai_category, categoryLabel),
                                        Toast.LENGTH_LONG
                                ).show();
                            } catch (Exception ignored) {
                                Toast.makeText(ReportActivity.this, R.string.report_saved, Toast.LENGTH_SHORT).show();
                            }
                            setResult(RESULT_OK);
                            finish();
                        });
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> {
                            submitButton.setEnabled(true);
                            Toast.makeText(ReportActivity.this, message, Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERM_REQUEST) {
            fetchLocation();
        }
    }
}
