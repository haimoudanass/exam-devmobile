package com.smartcity.app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public static String fixImageUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        return url
                .replace("http://localhost", ApiConfig.HOST_BASE)
                .replace("http://127.0.0.1:8000", ApiConfig.HOST_BASE);
    }

    public static Report parseReport(JSONObject item) throws Exception {
        return new Report(
                item.getLong("id"),
                item.getString("title"),
                item.optString("description", ""),
                item.optString("category", "general"),
                item.optString("status", "pending"),
                item.getDouble("latitude"),
                item.getDouble("longitude"),
                fixImageUrl(item.optString("image_url", null))
        );
    }

    public void login(String email, String password, ApiCallback<String> callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", password);

            Request request = new Request.Builder()
                    .url(ApiConfig.url("auth/login"))
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(wrapTokenCallback(callback));
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public void registerFcmToken(String authToken, String fcmToken, ApiCallback<Void> callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("fcm_token", fcmToken);

            Request request = new Request.Builder()
                    .url(ApiConfig.url("users/fcm-token"))
                    .addHeader("Authorization", "Bearer " + authToken)
                    .addHeader("Accept", "application/json")
                    .put(RequestBody.create(body.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        String raw = response.body() != null ? response.body().string() : "";
                        callback.onError("HTTP " + response.code() + ": " + raw);
                        return;
                    }
                    callback.onSuccess(null);
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public void fetchReports(String token, ApiCallback<List<Report>> callback) {
        fetchReports(token, null, callback);
    }

    public void fetchReports(String token, String statusFilter, ApiCallback<List<Report>> callback) {
        String url = ApiConfig.url("reports?per_page=50");
        if (statusFilter != null && !statusFilter.isEmpty()) {
            url += "&status=" + statusFilter;
        }

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code() + ": " + raw);
                    return;
                }
                try {
                    JSONObject json = new JSONObject(raw);
                    JSONArray data = json.getJSONArray("data");
                    List<Report> reports = new ArrayList<>();
                    for (int i = 0; i < data.length(); i++) {
                        reports.add(parseReport(data.getJSONObject(i)));
                    }
                    callback.onSuccess(reports);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void fetchReport(String token, long id, ApiCallback<Report> callback) {
        Request request = new Request.Builder()
                .url(ApiConfig.url("reports/" + id))
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("Accept", "application/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code() + ": " + raw);
                    return;
                }
                try {
                    callback.onSuccess(parseReport(new JSONObject(raw)));
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    public void createReport(
            String token,
            String title,
            String description,
            double latitude,
            double longitude,
            File imageFile,
            ApiCallback<JSONObject> callback
    ) {
        try {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("title", title)
                    .addFormDataPart("description", description != null ? description : "")
                    .addFormDataPart("latitude", String.valueOf(latitude))
                    .addFormDataPart("longitude", String.valueOf(longitude));

            if (imageFile != null && imageFile.exists()) {
                builder.addFormDataPart(
                        "image",
                        imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/*"))
                );
            }

            Request request = new Request.Builder()
                    .url(ApiConfig.url("reports"))
                    .addHeader("Authorization", "Bearer " + token)
                    .addHeader("Accept", "application/json")
                    .post(builder.build())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String raw = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        callback.onError("HTTP " + response.code() + ": " + raw);
                        return;
                    }
                    try {
                        callback.onSuccess(new JSONObject(raw));
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private Callback wrapTokenCallback(ApiCallback<String> callback) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String raw = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code() + ": " + raw);
                    return;
                }
                try {
                    JSONObject json = new JSONObject(raw);
                    callback.onSuccess(json.getString("token"));
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }
        };
    }
}
