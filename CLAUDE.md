# TICman — CLAUDE.md

## Project Overview

**TICman** (Team Interactive Client manager) — a collaborative, context-aware
REST client. Teams save HTTP requests alongside the *why* behind them —
annotations, gotchas, response history, and diffs.
Think Postman, but with shared team knowledge baked in.

## Stack

- **Frontend:** Quasar 2 (Vue 3, Composition API, TypeScript)
- **Backend:** Scala 3, Cask, uPickle, requests-scala (or sttp) — built with **sbt**
- **Database:** PostgreSQL 15 — run locally via **docker-compose**; migrations managed with **Flyway**
- **Auth:** JWT (access + refresh tokens), GitHub OAuth optional
- **Deployment:** Local dev only for now — no Kubernetes

## Repository Structure

```
/
├── frontend/          # Quasar/Vue app
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── stores/        # Pinia stores
│   │   └── composables/
├── backend/           # Scala backend
│   ├── src/
│   │   ├── routes/        # Cask route definitions
│   │   ├── services/      # Business logic
│   │   ├── models/        # uPickle-serializable case classes
│   │   └── db/            # ScalaSql queries
│   └── test/
├── db/
│   └── migrations/        # SQL migration files (sequential, e.g. 001_init.sql)
└── docker-compose.yml     # PostgreSQL for local dev
```

## Data Model (core tables)

```sql
users               (id, email, display_name, hashed_password, created_at)
workspaces          (id, name, owner_id, created_at)
workspace_members   (workspace_id, user_id, role)   -- role: owner | editor | viewer
collections         (id, workspace_id, name, readme, created_at)
requests            (id, collection_id, created_by, name, method, url_template,
                     headers, body, created_at, updated_at)
environments        (id, workspace_id, name)         -- dev, staging, prod
env_variables       (id, environment_id, user_id, key, value)  -- user-scoped secrets
response_history    (id, request_id, user_id, status_code, headers, body,
                     duration_ms, executed_at)
annotations         (id, request_id, user_id, content, created_at)
```

Key design notes:
- `url_template` and `body` support `{{variable}}` interpolation resolved at proxy time
- `env_variables` are **user-scoped** — shared collection, private credentials
- `response_history` enables diff view between any two responses

## Backend Conventions

- Routes are defined in `routes/` as Cask `cask.Routes` objects
- Business logic lives in `services/` — routes stay thin
- All JSON serialization via uPickle; derive ReadWriter for every model
- DB queries via ScalaSql — no raw string queries outside `db/`
- Auth middleware: validate JWT on every protected route, inject `userId` into handler
- Proxy endpoint forwards requests server-side to avoid CORS issues
- Return consistent error envelopes: `{"error": "message", "code": "ERROR_CODE"}`

## Frontend Conventions

- Vue 3 Composition API with `<script setup>` — no Options API
- Pinia for state; one store per domain (auth, workspaces, collections, requests)
- Quasar components preferred over custom CSS where possible
- API calls centralized in `src/api/` — no raw fetch calls in components
- Monaco editor for request body (JSON/plaintext)
- Environment variable interpolation previewed live in the URL bar

## Common Commands

```bash
# Database (start Postgres via Docker)
docker-compose up -d

# Backend
cd backend
sbt run           # start dev server (port 8080)
sbt test          # run tests

# Frontend
cd frontend
quasar dev        # start dev server (port 9000)
quasar build      # production build

# Apply migrations (Flyway picks up files from db/migrations/)
sbt flywayMigrate
```

## Auth Flow

1. `POST /api/auth/register` — create user, return JWT pair
2. `POST /api/auth/login` — validate credentials, return JWT pair
3. `POST /api/auth/refresh` — exchange refresh token for new access token
4. All protected routes expect `Authorization: Bearer <access_token>`
5. Access token TTL: 15 minutes. Refresh token TTL: 7 days.

## Proxy Endpoint

`POST /api/proxy` — forwards an HTTP request on behalf of the user.

Request body:
```json
{
  "method": "GET",
  "url": "https://api.example.com/users/{{user_id}}",
  "headers": { "Authorization": "Bearer {{api_key}}" },
  "body": null,
  "environmentId": "uuid"
}
```

The backend:
1. Resolves `{{variables}}` using the caller's `env_variables` for the given environment
2. Forwards the request using requests-scala
3. Saves the result to `response_history`
4. Returns status, headers, body, and duration

## Workspace & Permissions Rules

- Only workspace `owner` can add/remove members or delete the workspace
- `editor` can create/edit/delete collections and requests, add annotations
- `viewer` can view and execute requests, but cannot modify anything
- Enforce permissions in service layer, not just route layer

## Key Features — Build Order

1. **Auth** — register, login, JWT middleware
2. **Workspaces** — create, invite members, roles
3. **Collections & Requests** — CRUD, organized under workspace
4. **Proxy endpoint** — forward requests, save response history
5. **Environments & variables** — user-scoped, `{{interpolation}}`
6. **Annotations** — per-request notes from any team member
7. **Response diff view** — compare any two entries from history
8. **Activity feed** — recent changes across the workspace

## Testing Approach

- Backend: unit test services independently of routes; integration test the proxy
- Frontend: Vitest for composables and stores; Cypress for critical flows (login, send request)
- Always run `sbt test` before committing backend changes

## Environment Variables (backend)

```
DATABASE_URL=postgresql://...
JWT_SECRET=...
JWT_REFRESH_SECRET=...
PORT=8080
```

## What NOT to do

- Don't store `env_variables` values in `response_history` or logs
- Don't allow the proxy to reach internal/private IP ranges (SSRF protection)
- Don't put raw SQL strings outside `db/` layer
- Don't share access tokens in URLs — always use Authorization header
- Don't let route handlers contain business logic — delegate to services
