# Firebase Cloud Messaging (FCM)

## Files you must have

| File | Location |
|------|----------|
| `google-services.json` | `frontend/app/` |
| Service account key | `backend/storage/app/firebase-auth.json` (**required for push only**) |

> Without this file, the app still works (login, reports list, map). Push notifications are skipped until you add the JSON from Firebase Console → Project settings → Service accounts → Generate new private key.

## Backend

1. Add to `backend/.env`:

```env
FIREBASE_CREDENTIALS=storage/app/firebase-auth.json
```

2. Run migration:

```bash
cd backend
php artisan migrate
```

## Android

1. **Sync Gradle** in Android Studio (File → Sync Project with Gradle Files).
2. Run on a **physical device** or emulator with Google Play services.
3. Accept **notification permission** when prompted (Android 13+).

## API endpoints

| Method | Endpoint | Body |
|--------|----------|------|
| PUT | `/api/v1/users/fcm-token` | `{ "fcm_token": "..." }` |
| PUT | `/api/v1/reports/{id}/status` | `{ "status": "resolved" }` |

When status becomes `resolved`, the report owner receives a push notification.

## Test push (after login + FCM token saved)

```bash
# Login and get token, then resolve report id 1:
curl -X PUT http://127.0.0.1:8000/api/v1/reports/1/status \
  -H "Authorization: Bearer YOUR_SANCTUM_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"status\":\"resolved\"}"
```

Ensure the report owner has `fcm_token` set in the database (open the app once while logged in).
