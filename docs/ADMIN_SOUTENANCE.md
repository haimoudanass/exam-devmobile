# Admin Smart City — Soutenance (les 2 options)

## Architecture en une phrase

| Rôle | Interface | Accès |
|------|-----------|--------|
| **Citoyen** | App Android (S22) | Signaler, carte, distance, notifications |
| **Admin / Commune** | Dashboard Web **ou** Postman | Changer le statut → push FCM au citoyen |

---

## Option 1 — Dashboard Web (Full-stack)

### URL

- PC : `http://127.0.0.1:8000/admin/login`
- Téléphone / réseau local : `http://192.168.11.103:8000/admin/login` (adapter l’IP du PC)

### Comptes

| Rôle | Email | Mot de passe |
|------|-------|--------------|
| Admin | `admin@smartcity.local` | `admin123` |
| Citoyen (mobile) | `demo@smartcity.local` | `password123` |

### Utilisation

1. Se connecter en admin.
2. Voir le **Bento grid** (stats) + tableau des signalements.
3. Changer le statut via le menu déroulant → **Résolu** envoie une notification Firebase au citoyen (si `fcm_token` + `firebase-auth.json` configurés).

### Setup

```bash
cd backend
php artisan migrate
php artisan db:seed
php artisan storage:link
php artisan serve --host=0.0.0.0 --port=8000
```

---

## Option 2 — Simulation Postman (live demo mobile)

Idéal si le jury veut voir la **notification en temps réel** sur le S22 sans ouvrir le navigateur admin.

### Fichier

Importer : `docs/postman/SmartCity-API.postman_collection.json`

Variable `base_url` : `http://192.168.11.103:8000/api/v1`

### Scénario jury (2 minutes)

1. **S22** : app ouverte, connectée en `demo@smartcity.local`, FCM enregistré (ouvrir l’app une fois).
2. **Postman** : requête **1 — Admin login** → token admin sauvegardé.
3. **Postman** : requête **3 — Liste** → noter l’`id` d’un rapport `pending` du citoyen demo → mettre `report_id` dans les variables.
4. **Postman** : requête **6 — RÉSOUDRE** → `PUT .../reports/{id}/status` body `{ "status": "resolved" }`.
5. **S22** : notification « Votre signalement a été résolu » apparaît.

### Réponse jury : *« Comment l’admin gère les rapports ? »*

> « L’admin dispose d’un **dashboard web** (Laravel + session) pour filtrer et changer les statuts. La même logique est exposée via **API REST sécurisée** (`PUT /reports/{id}/status`, rôle admin uniquement). Lors du passage à *résolu*, le backend déclenche **Firebase Cloud Messaging** vers le token du citoyen. En démo live, on peut aussi simuler l’admin avec **Postman** pendant que le jury observe la notification sur le téléphone. »

---

## Sécurité API

- `PUT /api/v1/reports/{id}/status` → middleware **`admin`** (token Sanctum d’un compte `role=admin`).
- Un citoyen ne peut plus changer le statut d’un autre rapport via l’API.

---

## Future Work (rapport PFA)

- Rôles granulaires (agent quartier, superviseur).
- Carte admin web (Leaflet / Google Maps embed).
- Historique d’audit des changements de statut.
