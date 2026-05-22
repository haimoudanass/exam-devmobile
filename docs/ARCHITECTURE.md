# Smart City App — Architecture

## Overview

Citizens report urban issues (potholes, lighting, waste) with geolocation and photos. Admins track status via API.

| Layer | Stack |
|-------|--------|
| API | Laravel 12 + Sanctum |
| Database | PostgreSQL (`smartcity`) |
| Mobile | Android (Java + XML) |

## Database schema

### `users` (Laravel default + API tokens)

| Column | Type |
|--------|------|
| id | bigint PK |
| name | string |
| email | string unique |
| password | string (hashed) |
| email_verified_at | timestamp nullable |
| remember_token | string nullable |
| created_at / updated_at | timestamps |

### `reports`

| Column | Type |
|--------|------|
| id | bigint PK |
| user_id | FK → users |
| title | string |
| description | text nullable |
| latitude | decimal(10,7) |
| longitude | decimal(10,7) |
| image_path | string nullable |
| status | enum: `pending`, `in_progress`, `resolved` |
| created_at / updated_at | timestamps |

## REST API (`/api/v1`)

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/auth/register` | No |
| POST | `/auth/login` | No |
| POST | `/auth/logout` | Bearer token |
| GET | `/user` | Bearer token |
| GET | `/reports` | Bearer token |
| POST | `/reports` | Bearer token (multipart for image) |
| GET | `/reports/{id}` | Bearer token |
| PUT/PATCH | `/reports/{id}` | Bearer token (owner) |
| DELETE | `/reports/{id}` | Bearer token (owner) |

## Android screens

1. **LoginActivity** — auth entry
2. **HomeActivity** — dashboard / report list
3. **MapActivity** — map of reports
4. **ReportActivity** — create report (title, description, location, photo)

## Mobile → API

- Base URL (emulator): `http://10.0.2.2:8000/api/v1`
- Header: `Authorization: Bearer {token}`
