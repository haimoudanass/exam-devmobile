package com.smartcity.app;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SmartCityFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "SmartCityFCM";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = getString(R.string.app_name);
        String body = "";

        if (message.getNotification() != null) {
            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }
            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        if (body.isEmpty() && message.getData().containsKey("body")) {
            body = message.getData().get("body");
        }

        if ("report_resolved".equals(message.getData().get("type"))) {
            title = getString(R.string.notif_resolved_title);
            String reportTitle = message.getData().get("report_title");
            if (reportTitle != null && !reportTitle.isEmpty()) {
                body = getString(R.string.notif_resolved_body, reportTitle);
            } else if (body.isEmpty()) {
                body = getString(R.string.notif_resolved_body, "");
            }
        }

        if (!body.isEmpty()) {
            NotificationHelper.showMessage(this, title, body);
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "FCM token refreshed");
        FcmRegistrar.syncTokenIfLoggedIn(getApplicationContext(), token);
    }
}
