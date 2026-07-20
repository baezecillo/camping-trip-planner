# Running camping-trip-planner

This guide covers everything needed to run the full application
(frontend + backend + database) locally, plus how to reproduce the
automated build loops used during development (optional, for grading
transparency).

## Prerequisites

- Docker Desktop, with WSL2 integration enabled (if on Windows)
- Node.js 18+ (only needed to run the frontend dev server; the backend
  runs entirely inside Docker and does not require a local JDK)
- A Google Cloud account (free) to generate a Maps Embed API key — see
  "Google Maps API Key" below. The app will run without one, but Screen
  2's map will show an "invalid API key" error.

> Note: a local JDK/Gradle install is **not** required to run the app —
> the backend builds inside its own Docker container. A JDK is only
> needed if you want to run `./gradlew build test` directly on the host
> (e.g. to reproduce the build loop outside Docker).

## 1. Clone the repository

```bash
git clone https://github.com/baezecillo/camping-trip-planner.git
cd camping-trip-planner
```

## 2. Configure environment variables

Two separate `.env` files are needed — neither is committed to the repo
(both are git-ignored); `.env.example` files show the expected shape.

**Root `.env`** (MySQL credentials, used by Docker Compose and the
backend):

```bash
cat > .env << 'EOF'
MYSQL_ROOT_PASSWORD=changeme_root
MYSQL_DATABASE=campingtrip
MYSQL_USER=campingapp
MYSQL_PASSWORD=changeme_app
EOF
```

**`frontend/.env`** (Google Maps key):

```bash
cd frontend
cat > .env << 'EOF'
VITE_GOOGLE_MAPS_KEY=your_real_key_here
EOF
cd ..
```

### Getting a Google Maps API key

1. Go to console.cloud.google.com, create or select a project.
2. Go to APIs & Services -> Library, search for **Maps Embed API**
   specifically (not the JS SDK), and enable it.
3. Go to APIs & Services -> Credentials -> Create Credentials -> API key.
4. Restrict the key: API restrictions -> only "Maps Embed API"; under
   Application restrictions -> Websites -> add `http://localhost:5173/*`.
5. Paste the key into `frontend/.env` as shown above.

You can sanity-check a key independently of the app by pasting this URL
directly into a browser tab:

```text
https://www.google.com/maps/embed/v1/directions?key=YOUR_KEY&origin=Pittsburgh,PA&destination=Cook+Forest+State+Park,PA&mode=driving
```

If that alone renders a route, the key is valid.

## 3. Start the backend + database

From the repo root:

```bash
docker compose up --build
```

This builds the backend image, starts MySQL, waits for MySQL's
healthcheck to pass before starting the backend (so there's no race
condition), and runs Flyway migrations automatically on backend startup.

Confirm it's up:

```bash
curl -i http://localhost:8080/api/trips/current
```

(Expect a `401` — no session yet — which confirms the server is
responding.)

To reset the database to a clean state at any point:

```bash
docker compose down -v
docker compose up --build
```

## 4. Start the frontend

In a separate terminal, from the repo root:

```bash
cd frontend
npm install
npm run dev
```

Open the URL Vite prints (default: `http://localhost:5173`).

## 5. Using the app

1. Register a new account, then log in.
2. On the search screen, fill in **From**, **Where**, and a date range,
   then click **Go**.
3. You'll see the trip detail screen: an embedded driving-route map, a
   countdown to your start date, and a packing checklist grouped by
   category. Check items off as needed — each toggle is saved
   immediately.
4. Click **Wrap Up** at the top to delete the trip and return to the
   search screen.

## Troubleshooting

| Symptom                                                                         | Likely cause                                                                                                              |
| ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------- |
| Frontend can't log in / requests silently fail in browser (but curl works)      | CORS — confirm the backend was rebuilt after the CORS fix (`docker compose up --build`)                                   |
| "Google Maps Platform rejected your request. The provided API key is invalid."  | `frontend/.env` missing/wrong key, or the dev server wasn't restarted after adding it — Vite only reads `.env` at startup |
| Backend fails to start / Flyway errors on a fresh `docker compose up`           | Try `docker compose down -v` first — a previous failed run may have left MySQL's volume in a bad state                    |
| `gradlew: Permission denied` (only relevant if running Gradle directly on host) | Run `chmod +x gradlew`                                                                                                    |

## Reproducing the Build Loops (optional — for grading transparency)

The backend and frontend were originally built using automated
build-verify-repeat loops driven by the Claude Code CLI, not manual
prompting. To reproduce:

```bash
# Requires the Claude Code CLI installed and authenticated (claude --version)
./scripts/build-loop.sh "$(cat prompts/step1-base-system/01-build-initial-task.txt)"
./scripts/frontend-build-loop.sh "$(cat prompts/step1-base-system/02-build-frontend-initial-task.txt)"
```

Each run appends its prompts to `prompts.txt` and a per-iteration log to
`docs/step1-base-system/03-build.md` and
`docs/step1-base-system/build-loop-raw.jsonl`. See
`docs/step1-base-system/03-build.md` for the loop's design and a
description of what each script's stop/safety-cap conditions are.
