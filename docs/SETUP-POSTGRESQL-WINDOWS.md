# PostgreSQL on Windows (XAMPP / PHP 8.2)

Migrations failed with **"could not find driver"** because `pdo_pgsql` is not enabled.

## 1. Enable PHP extension

Edit `php.ini` (e.g. `C:\xampp\php\php.ini`):

```ini
extension=pdo_pgsql
extension=pgsql
```

Restart terminal, then verify:

```bash
php -m | findstr pgsql
```

## 2. PostgreSQL server

- Install PostgreSQL if needed.
- Create database: `CREATE DATABASE smartcity;`
- Set `DB_PASSWORD` in `backend/.env`.

## 3. Run migrations

```bash
cd backend
php artisan migrate
```

Until then, the API code and `.env` are ready; only the DB connection is pending.
