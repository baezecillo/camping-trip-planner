# Step 2 — Extension: Design

Save this file at: `docs/step2-extension/02-design.md`

## Providers

- **Geocoding:** `https://geocoding-api.open-meteo.com/v1/search?name={destination}&count=1`
  Returns a `results` array; take `results[0].latitude` /
  `results[0].longitude`. If `results` is empty or missing, geocoding
  failed — treat as unavailable (see below). No API key.

- **Forecast:** `https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lng}&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max&forecast_days=16&timezone=auto`
  Returns a `daily` object with parallel arrays: `time` (dates),
  `weather_code`, `temperature_2m_max`, `temperature_2m_min`,
  `precipitation_probability_max`. Find the index in `daily.time` matching
  the trip's `start_date`; if the date isn't present in the array
  (because it's beyond the 16-day horizon), forecast is unavailable for
  that date. No API key.

## "Unavailable" Cases (all resolved server-side, all resulting in the same frontend behavior — a friendly fallback message)

1. Trip's `start_date` is more than 16 days from today (per FR4/FR5,
   resolved in Specify stage).
2. Geocoding returns zero results for the destination string (a real
   risk: the geocoder is built for place names/cities, and a specific
   destination like "Cook Forest State Park, PA" may not resolve
   cleanly — this needs to degrade gracefully, not error).
3. The forecast API call itself fails (timeout, non-200, malformed
   response) — network issues with an external service should never
   surface as a 500 to the frontend.

All three cases return the same response shape (see API Contract below)
so the frontend only needs to branch on one boolean, not on which
specific thing went wrong.

## API Contract

### `GET /api/trips/current/weather`

Requires an active session and an active trip (same auth as other trip
endpoints).

**Success response** (`200 OK`):

```json
{
  "available": true,
  "date": "2026-08-01",
  "weatherCode": 3,
  "temperatureMaxC": 24.5,
  "temperatureMinC": 15.2,
  "precipitationProbabilityMax": 20
}
```

**Unavailable response** (`200 OK` — not an error status, since "no
forecast yet" is an expected, normal case, not a failure of the request
itself):

```json
{
  "available": false
}
```

**Errors:**

- `401 Unauthorized` — no session
- `404 Not Found` — no active trip (same as other trip endpoints)

Note: geocoding/forecast-provider failures do **not** produce a 5xx here
— per the "Unavailable Cases" above, they collapse into the same
`{"available": false}` response. This is a deliberate design choice: the
weather feature failing should never look different, from the frontend's
perspective, than the weather feature simply not having data yet.

## Backend Implementation Notes

- New `WeatherService` (or similar) called from a new controller method,
  not embedded in `TripService` — keeps trip CRUD and weather-fetching
  as separate concerns, since one is core trip data and the other is a
  live external lookup.
- `weatherCode` is returned as Open-Meteo's raw WMO code (an integer);
  translating that to a human-readable description/icon is a **frontend**
  concern, not backend, so the backend contract doesn't need to hardcode
  a code→description mapping that might need updating later.
- HTTP client calls to both external APIs should have an explicit
  timeout (e.g. 5s) — without one, a slow/hanging external API could
  make trip-detail loading hang indefinitely, which would violate NFR3.
- No new database table or migration — nothing here is persisted.

## Frontend Design (NFR3: failure isolation)

The Weather card fetches independently of the main trip fetch — it does
**not** block or get bundled into the `GET /api/trips/current` call that
loads the map/countdown/checklist. Concretely:

- `TripDetailScreen` renders `WeatherCard` as a child component that
  manages its own fetch lifecycle (its own loading/error/data state),
  triggered on mount using the already-loaded trip's `destination` and
  `startDate` as props.
- If `WeatherCard`'s fetch fails outright (network error, unexpected
  shape) — as opposed to a clean `{"available": false}` response — it
  renders a quiet fallback ("Weather unavailable right now") rather than
  throwing or blocking the rest of the screen. This is a second layer of
  isolation on top of the backend's own graceful degradation: even if the
  backend's error handling somehow has a gap, the frontend doesn't trust
  it blindly.

### Updated Component Breakdown

```text
TripDetailScreen
├── WrapUpButton
├── RouteMap
├── Countdown
├── WeatherCard          (NEW — independent fetch, isolated failure)
└── Checklist
    └── ChecklistItem
```

## Test Plan for Build Stage

The build-loop's verification needs concrete, checkable cases — not just
"does it compile":

- A trip within 16 days returns a real forecast (mock the Open-Meteo
  response in the test, don't hit the live API in automated tests)
- A trip beyond 16 days returns `{"available": false}`
- Geocoding returning zero results returns `{"available": false}`, not
  an error
- A simulated forecast-API failure (mocked timeout/500) returns
  `{"available": false}`, not a 500 propagated to the caller
