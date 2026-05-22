package com.smartcity.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        apiClient = new ApiClient();

        if (sessionManager.isLoggedIn()) {
            goHome();
            return;
        }

        setContentView(R.layout.activity_login);

        EditText emailInput = findViewById(R.id.inputEmail);
        EditText passwordInput = findViewById(R.id.inputPassword);
        Button loginButton = findViewById(R.id.btnLogin);
        TextView demoHint = findViewById(R.id.textDemoHint);

        if (demoHint != null) {
            demoHint.setText(R.string.demo_hint);
        }

        emailInput.setText("demo@smartcity.local");
        passwordInput.setText("password123");

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.login_error_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            loginButton.setEnabled(false);
            Toast.makeText(this, R.string.login_loading, Toast.LENGTH_SHORT).show();

            apiClient.login(email, password, new ApiClient.ApiCallback<String>() {
                @Override
                public void onSuccess(String token) {
                    runOnUiThread(() -> {
                        sessionManager.saveToken(token);
                        FcmRegistrar.register(LoginActivity.this);
                        goHome();
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        loginButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, R.string.login_failed + ": " + message, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
