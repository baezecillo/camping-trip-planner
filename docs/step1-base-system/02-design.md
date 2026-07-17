# Step 1 — Base System: Design

Save this file at: `docs/step1-base-system/02-design.md`

## Decisions Carried Over from Specify

- **Auth mechanism:** Session-based auth via Spring Security, using an
  HTTP session cookie. No JWT — simpler for a course-scope project, no
  token refresh/storage logic needed on the frontend.
- **Checklist defaults:** Fixed, hardcoded default list seeded on trip
  creation (see below). Editing/customizing the list is out of scope for
  Step 1.

## Data Model (SQL DDL)

```sql
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trips (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    origin      VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_trips_one_active_per_user UNIQUE (user_id)
);

CREATE TABLE checklist_items (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id   BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category  VARCHAR(50)  NOT NULL,
    is_packed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_checklist_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
        ON DELETE CASCADE
);
```

Note: `uq_trips_one_active_per_user` enforces "one trip at a time" directly
at the database level — a second `INSERT` for the same user fails, so the
backend doesn't need to re-check this itself before creating.

## Default Checklist Seed

Seeded server-side on trip creation (`TripService.createTrip(...)`):

| Category           | Items                                                               |
| ------------------ | ------------------------------------------------------------------- |
| Shelter & Sleeping | Tent, Sleeping bag, Sleeping pad, Pillow                            |
| Cooking & Food     | Camp stove, Cooler, Water container, Matches/lighter, Food & snacks |
| Clothing           | Rain jacket, Warm layer, Extra socks, Sturdy shoes                  |
| Tools & Safety     | First aid kit, Flashlight/headlamp, Multi-tool, Map/compass         |

(16 default items across 4 categories — kept intentionally short for a
course project; easy to expand later without a schema change.)

## API Contract

All endpoints under `/api`. All trip/checklist endpoints require an active
session (401 if not logged in).

### `POST /api/auth/register`

Request: `{ "username": "string", "password": "string" }`
Response: `201 Created`, `{ "id": 1, "username": "string" }`

### `POST /api/auth/login`

Request: `{ "username": "string", "password": "string" }`
Response: `200 OK` + session cookie set. Body: `{ "username": "string" }`
Errors: `401 Unauthorized` on bad credentials.

### `POST /api/auth/logout`

Response: `204 No Content`, session invalidated.

### `POST /api/trips`

Request: `{ "origin": "string", "destination": "string", "startDate": "YYYY-MM-DD", "endDate": "YYYY-MM-DD" }`
Response: `201 Created`, full trip object with seeded checklist (see GET below).
Errors: `409 Conflict` if user already has an active trip.

### `GET /api/trips/current`

Response: `200 OK`:

```json
{
  "id": 1,
  "origin": "Pittsburgh, PA",
  "destination": "Cook Forest State Park, PA",
  "startDate": "2026-08-01",
  "endDate": "2026-08-03",
  "daysUntilStart": 15,
  "checklist": [
    { "id": 1, "itemName": "Tent", "category": "Shelter & Sleeping", "isPacked": false }
  ]
}
```

Errors: `404 Not Found` if user has no active trip (frontend shows Screen 1).

### `PATCH /api/checklist/{id}`

Request: `{ "isPacked": true }`
Response: `200 OK`, updated item.
Errors: `403 Forbidden` if the item doesn't belong to the caller's trip.

### `DELETE /api/trips/current`

Response: `204 No Content`. Deletes the trip; `ON DELETE CASCADE` removes
its checklist items automatically.

## Frontend Component Breakdown

```Plain
App
├── LoginPage            (username/password form, calls /api/auth/login)
├── RegisterPage
└── TripPlanner           (parent, fetches GET /api/trips/current on mount)
    ├── SearchScreen       (shown if 404 — From/Where/When/Go)
    └── TripDetailScreen   (shown if trip exists)
        ├── WrapUpButton
        ├── RouteMap        (Google Maps Embed iframe)
        ├── Countdown
        └── Checklist
            └── ChecklistItem (checkbox, calls PATCH on toggle)
```

State lives in `TripPlanner` (current trip + checklist), passed down as
props. No global state library needed at this scope — plain `useState`
and one fetch on mount is sufficient.

## Sequence: Wrap Up

1. User clicks Wrap Up.
2. Frontend calls `DELETE /api/trips/current`.
3. On `204`, frontend clears local trip state and renders `SearchScreen`.

## Open Items Resolved from Specify Stage

- ✅ Auth mechanism → session-based
- ✅ Checklist defaults → fixed 16-item list, 4 categories
- ✅ "From"/"Where" validation → deferred to Build stage; Embed API will
  simply fail to render a route gracefully if a place name isn't
  resolvable, so no separate validation service is needed for Step 1
