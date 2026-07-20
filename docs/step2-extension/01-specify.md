# Step 2 — Extension: Specify

Save this file at: `docs/step2-extension/01-specify.md`

## Overview

Extend the trip detail screen (Screen 2) with a weather forecast for the
destination at the trip's start date. Since weather APIs only forecast
reliably a limited number of days out, the feature must degrade
gracefully for trips further in the future rather than erroring or
showing stale/fabricated data.

## User Flow

1. User has an active trip (Screen 2 already showing map, countdown,
   checklist, per Step 1).
2. A new Weather card appears on Screen 2.
3. If the trip's start date is within the forecast provider's reliable
   range: the card shows the forecast (conditions, temperature,
   precipitation chance) for the destination on the start date.
4. If the trip's start date is beyond that range: the card shows a
   friendly message (e.g. "Forecast available closer to your trip") — not
   an error, not a blank space, not fabricated data.
5. Weather data is fetched live on each page load — it is not stored in
   the database, since forecasts change daily and stale cached weather
   would be actively misleading.

## Functional Requirements

- FR1: Add `GET /api/trips/current/weather`, returning either a forecast
  payload or an "unavailable" flag — never a 500 or empty response for
  the case where the trip is simply too far out.
- FR2: The backend resolves the trip's destination (a free-text string,
  e.g. "Cook Forest State Park, PA") to coordinates via a geocoding step,
  since the forecast provider requires lat/lng, not place names.
- FR3: The backend calls the forecast provider using those coordinates
  and the trip's start date.
- FR4: If the start date falls within the provider's supported forecast
  horizon, return the forecast (temperature, conditions/weather code,
  precipitation probability) for that date.
- FR5: If the start date falls outside that horizon, return
  `{"available": false}` rather than attempting a best-guess or
  historical-average substitute.
- FR6: The frontend renders a Weather card on Screen 2: real forecast
  data when available, a clear "check back closer to your trip" message
  otherwise.
- FR7: No new database table or persisted weather data — always fetched
  live per request.

## Non-Functional Requirements

- NFR1: No API key management required for this feature (Open-Meteo:
  free, keyless) — keeps this consistent with Step 1's public-repo-safe
  approach to the Google Maps key, but without even that level of
  credential handling needed here.
- NFR2: Weather API calls happen server-side, not directly from the
  frontend — consistent with how the Google Maps key is handled
  client-side only because it's designed to be publicly embeddable, while
  this integration has no such requirement and should stay backend-only.
- NFR3: A failure of the external weather/geocoding service (e.g.
  timeout, unexpected response shape) must not break the rest of Screen
  2 — the map, countdown, and checklist must continue to render
  regardless of weather-fetch success or failure.

## Out of Scope for Step 2

- Historical weather / "typical weather for this time of year" as a
  fallback for far-future trips
- Multi-day forecast (only the single start-date forecast is required)
- Weather-based packing list suggestions (e.g. auto-suggesting a rain
  jacket if rain is forecast) — a natural future idea, but not required
  here
- Caching/rate-limit handling for the weather API beyond what's needed
  for normal single-user course-project usage

## Open Questions for Design Stage

- ~~Exact forecast provider + geocoding endpoint choice and their specific~~
  ~~documented forecast horizon~~ **Resolved:** Open-Meteo's Weather
  Forecast API (`/v1/forecast`, `best_match` model) supports up to 16
  days of forecast when called with `forecast_days=16` explicitly (the
  endpoint defaults to 7 days if that parameter is omitted — must be set
  explicitly). FR4/FR5's boundary is: a trip is "within range" if its
  `start_date` falls within 16 days of today. Verified against current
  Open-Meteo documentation, not assumed from memory.
- Exact response shape for both the "available" and "unavailable" cases
- How `NFR3` failure isolation is implemented on the frontend (e.g. does
  the Weather card fetch independently of the main trip fetch, so its
  failure can't block the rest of the screen from rendering)
