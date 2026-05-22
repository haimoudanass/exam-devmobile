# Smart City App — Code Freeze

**Status: FROZEN** (after Step 7)

Development is complete. No further feature work unless explicitly requested for post-demo fixes.

## Delivered scope

| Area | Features |
|------|----------|
| Backend | Laravel 12 API, SQLite (dev), Sanctum auth, reports CRUD, AI category classifier, Casablanca seeder |
| Android | Login, bottom nav, dashboard list + SwipeRefresh, status filters, map + markers, bottom sheet, report detail, camera + GPS upload |
| UI/UX | Glassmorphism, light/dark mode (DayNight), color-coded category & status badges |
| FCM | Push on report resolved, device token registration (post-freeze addendum) |
| Gemini Vision | Image classification on `POST /reports` via `GEMINI_API_KEY` (post-freeze addendum) |

## Demo credentials

- Email: `demo@smartcity.local`
- Password: `password123`

## Run before soutenance

```bash
cd backend && php artisan migrate:fresh --seed && php artisan serve
```

Android: set `MAPS_API_KEY` in `frontend/local.properties`, open in Android Studio, run emulator.

## PostgreSQL switch (soutenance)

Uncomment PostgreSQL block in `backend/.env`, set `DB_PASSWORD`, run `php artisan migrate:fresh --seed`.
