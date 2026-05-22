package com.smartcity.app;

public final class ApiConfig {
    /**
     * Physical device: PC LAN IP (same Wi‑Fi). Emulator: use http://10.0.2.2:8000
     */
    public static final String HOST_BASE = "http://192.168.11.103:8000";

    /** No trailing slash — use {@link #url(String)} for every API path. */
    public static final String BASE_URL = HOST_BASE + "/api/v1";

    private ApiConfig() {}

    /** Builds a path without double slashes (e.g. auth/login → …/api/v1/auth/login). */
    public static String url(String path) {
        if (path == null || path.isEmpty()) {
            return BASE_URL;
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        return BASE_URL + "/" + path;
    }
}
