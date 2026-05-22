package com.smartcity.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_OPEN_MAP = "open_map";

    private static final String TAG_HOME = "home";
    private static final String TAG_MAP = "map";

    private BottomNavigationView bottomNav;
    private final ActivityResultLauncher<Intent> reportLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    refreshHome();
                    bottomNav.setSelectedItemId(R.id.nav_home);
                }
            });

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> { });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!new SessionManager(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNav);

        NotificationHelper.ensureChannel(this);
        requestNotificationPermissionIfNeeded();
        FcmRegistrar.register(this);

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(EXTRA_OPEN_MAP, false)) {
                showFragment(new MapFragment(), TAG_MAP);
                bottomNav.setSelectedItemId(R.id.nav_map);
            } else {
                showFragment(new HomeFragment(), TAG_HOME);
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new HomeFragment(), TAG_HOME);
                return true;
            }
            if (id == R.id.nav_map) {
                showFragment(new MapFragment(), TAG_MAP);
                return true;
            }
            if (id == R.id.nav_report) {
                reportLauncher.launch(new Intent(this, ReportActivity.class));
                return false;
            }
            return false;
        });
    }

    private void showFragment(@NonNull Fragment fragment, @NonNull String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment, tag)
                .commit();
    }

    private void refreshHome() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
        if (fragment instanceof HomeFragment) {
            ((HomeFragment) fragment).loadReports();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }
}
