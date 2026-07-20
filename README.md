# camping-trip-planner

A camping trip planner that reveals itself progressively: start with a
minimal search bar, and once you set a trip, get a driving-route map, a
countdown, a live weather forecast, and a packing checklist — all backed
by a real database, one trip at a time.

Built as a course project exploring **Loop Engineering**: using an AI
coding agent (Claude Code) inside automated build-verify-repeat loops,
rather than manual one-off prompting, to implement and self-correct real
features.

## Features

- **Progressive UI:** a minimal From / Where / When search screen, then
  a full trip detail view once a trip exists.
- **Route map:** embedded Google Maps driving directions from your
  starting point to the destination.
- **Countdown:** days remaining until the trip starts.
- **Weather forecast:** live forecast for the destination on the trip's
  start date (via Open-Meteo, no API key required), with a graceful
  fallback for trips too far in the future to forecast.
- **Packing checklist:** a default checklist seeded per trip, grouped by
  category, with persisted check/uncheck state.
- **Wrap Up:** clears the active trip and its checklist when you're done.
- **Session-based auth:** one active trip per user at a time.

## Tech Stack

- **Frontend:** React + Vite
- **Backend:** Spring Boot (Java 21, Gradle)
- **Database:** MySQL, schema managed by Flyway
- **Infrastructure:** Docker Compose
- **External APIs:** Google Maps Embed API, Open-Meteo (forecast +
  geocoding)

## Getting Started

See **[running.md](./running.md)** for full setup instructions,
including environment variables and Google Maps API key setup. Short
version:

```bash
docker compose up --build      # backend + MySQL
cd frontend && npm install && npm run dev   # frontend dev server
```

## Repository Structure

```text
.
├── backend/            Spring Boot API (Java, Gradle)
├── frontend/            React + Vite single-page app
├── docs/                 Specify/Design/Build/Review docs per step
│   ├── step1-base-system/     Login, search, map, checklist, wrap up
│   ├── step2-extension/       Weather forecast feature
│   ├── stepX-manual-testing/  Manual verification evidence
│   └── ui-polish/             Style guide + visual polish tasks
├── prompts/              Curated prompts given to the coding agent,
│                          organized to mirror docs/
├── scripts/              Loop-engineering driver scripts
│   ├── build-loop.sh            Backend build-verify-repeat loop
│   ├── frontend-build-loop.sh   Frontend build-verify-repeat loop
│   └── single-shot.sh           One-off agent tasks (still logged)
├── prompts.txt            Full raw log of every prompt sent to the agent
├── running.md             How to run this project
└── reflection.md          A software-engineering lesson from this project
```

## How This Was Built

This project was built in two steps, each carried through a
specify → design → build → review cycle, documented under `docs/`:

1. **Step 1 — Base System:** login, trip search, route map, countdown,
   and packing checklist.
2. **Step 2 — Extension:** live weather forecast for the destination.

The **Build** stage for both steps used an automated loop
(`scripts/build-loop.sh` / `scripts/frontend-build-loop.sh`): the agent
implements, the script independently runs the real build/test suite, and
on failure the exact failure output is fed back into the next prompt —
repeating until it genuinely passes or a safety cap is hit. Every prompt
sent is logged to `prompts.txt`; every iteration's outcome is logged to
each step's `03-build.md` and a raw `build-loop-raw.jsonl`.

See `docs/step1-base-system/03-build.md` for the loop's full design
rationale, and `reflection.md` for what turned out to be the most
interesting lesson from the exercise.
