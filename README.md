# Smart City App

Laravel API + native Android client for citizen issue reporting.

## Project structure

```
SmartCityApp/
├── backend/          # Laravel 12 API
├── frontend/         # Android (Java/XML)
└── docs/             # Architecture & API notes
```

## Backend setup (SQLite — fast dev)

```bash[backend](backend)
cd backend[backend](backend)
php artisan migrate:fresh --seed
php artisan storage:link
php artisan serve
```

Demo account: `demo@smartcity.local` / `password123` (5 seeded reports in Casablanca).

**Before soutenance:** switch `backend/.env` back to PostgreSQL (see commented block in `.env`).

API: `http://127.0.0.1:8000/api/v1`

## Android setup

1. Open `frontend/` in Android Studio.
2. Copy `frontend/local.properties.example` → `frontend/local.properties` and set `MAPS_API_KEY` (Google Cloud Console → Maps SDK for Android).
3. Sync Gradle and run on emulator.
4. API base URL: `http://10.0.2.2:8000/api/v1` (`ApiConfig.java`).

### Step 5–6 features

- **ReportActivity:** camera intent, GPS, multipart upload + AI category from API
- **MainActivity:** bottom navigation (Home / Map / Report)
- **Home:** RecyclerView + SwipeRefreshLayout + Glide thumbnails
- **Map:** markers + BottomSheet detail on tap
- **ReportDetailActivity:** full report view from list or map
- **Login:** `demo@smartcity.local` / `password123`

### Step 7 — Final polish (code freeze)

- **Color badges:** category (safety=red, lighting=yellow, waste=green, water=blue, …) + status (pending, in progress, resolved)
- **Filters:** horizontal chips on Home (All / Pending / Resolved)
- **Dark mode:** `values-night` palettes — toggle via system theme on device/emulator

See `docs/CODE_FREEZE.md`.

### Firebase Cloud Messaging

See `docs/FCM_SETUP.md` — Gradle sync, migration, `firebase-auth.json`, push on report resolved.

## Quick API test

```bash
curl -X POST http://127.0.0.1:8000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"Test\",\"email\":\"test@smartcity.local\",\"password\":\"password123\",\"password_confirmation\":\"password123\"}"
```
