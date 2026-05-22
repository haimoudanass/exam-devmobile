package com.smartcity.app;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;

public final class FcmRegistrar {

    private static final String TAG = "FcmRegistrar";

    private FcmRegistrar() {}

    public static void register(Context context) {
        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn()) {
            return;
        }

        try {
            String fcmToken = Tasks.await(
                    FirebaseMessaging.getInstance().getToken(),
                    15,
                    TimeUnit.SECONDS
            );
            syncTokenIfLoggedIn(context, fcmToken);
        } catch (Exception e) {
            Log.w(TAG, "Unable to fetch FCM token: " + e.getMessage());
        }
    }

    public static void syncTokenIfLoggedIn(Context context, String fcmToken) {
        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn() || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        new ApiClient().registerFcmToken(session.getToken(), fcmToken, new ApiClient.ApiCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "FCM token registered with API");
            }

            @Override
            public void onError(String message) {
                Log.w(TAG, "FCM token registration failed: " + message);
            }
        });
    }
}
