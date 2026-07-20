# Step 2 — Extension: Review

Save this file at: `docs/step2-extension/04-review.md`

## Verification Summary

Same three-layer approach used for Step 1:

1. **Automated (backend):** `scripts/build-loop.sh` against
   `docs/step2-extension` — passed on iteration 1 (13 tests, including
   the 4 required Test Plan scenarios from `02-design.md` plus auth
   coverage).

2. **Automated (frontend):** `scripts/frontend-build-loop.sh` against
   `docs/step2-extension` — passed on iteration 1 (10/10 tests, including
   the NFR3 isolation test proving RouteMap/Countdown/Checklist still
   render when the weather fetch fails outright).

3. **Manual, real browser, real external APIs:**
   - Initial manual test (Pittsburgh → Cook Forest State Park, PA, start
     date within 16 days) surfaced a real bug the automated tests missed
     entirely: the WeatherCard showed "Forecast available closer to your
     trip" even though the trip was well within range.
   - Root-caused via direct `curl` against the live Open-Meteo geocoding
     API (not guessed): the exact destination string, including the
     `, PA` suffix, returned zero results — and notably the response
     had no `results` field at all in that case, not an empty array.
     Removing the state suffix resolved correctly.
   - Fixed via a second loop run (`03-fix-geocoding-fallback.txt`):
     comma-truncation retry, null-safe handling of a missing `results`
     field, and — since the complete absence of application logging is
     what made this hard to diagnose in the first place — new SLF4J
     logging added to `WeatherService`.
   - Re-tested manually: `docker compose logs backend` now shows the
     full geocoding decision trail (initial attempt → no results →
     truncated retry → success), and the WeatherCard renders real
     forecast data for the same trip that previously failed.

This is the clearest example across both steps of why automated
tests + curl are not sufficient on their own: the original backend
test suite (mocking the geocoding response) passed cleanly, because the
mock never exercised the *actual* response shape Open-Meteo returns for
a real multi-word destination with a state suffix. Only a real API call
against real data surfaced it.

## Requirements Traceability (against Step 2's 01-specify.md)

- FR1–FR5 (weather endpoint + graceful degradation): verified, including
  the geocoding-failure degradation path specifically, now via a real
  destination string rather than only a mocked scenario.
- FR6 (frontend Weather card): verified in-browser, both the
  available-forecast and unavailable-fallback rendering paths.
- FR7 (no persistence): unchanged, confirmed via design — weather is
  fetched live each time, never written to the database.
- NFR1 (no API key management): confirmed — Open-Meteo required no key
  for either the geocoding or forecast calls.
- NFR2 (backend-only external calls): confirmed — frontend only ever
  calls the backend's own `/api/trips/current/weather`, never Open-Meteo
  directly.
- NFR3 (weather failure doesn't break the rest of Screen 2): verified
  two ways — an automated test forcing a fetch failure and asserting the
  rest of the screen still renders, and structurally by the WeatherCard
  managing its own independent fetch lifecycle.

## Known Issues / Discrepancies Found During the Loop

1. **Geocoding fails on "Place Name, State" formatted destinations for
   non-city places** (found and fixed — see Verification Summary above).
   Residual risk: the comma-truncation fallback handles the common case
   but isn't a complete solution — a destination like
   "Old Faithful, Yellowstone National Park, WY" would still need
   more than one level of truncation to resolve, and currently only
   retries once. Documented here as a known limitation rather than
   silently left unaddressed.

2. **Backend had zero application-level logging prior to this bug.**
   Fixed as part of the same task, but worth flagging as a process
   lesson: the original task prompts never explicitly required logging,
   so the agent never added any — a gap that made the first diagnosis
   pass slower than it needed to be.

3. **Double-fetch observed in development** (`WeatherService` logs show
   the same request pattern twice, ~1 second apart, on a single page
   load). This is very likely React StrictMode intentionally
   double-invoking effects in development mode only — not reproduced in
   a production build (`npm run build && npm run preview`). Documented
   here rather than treated as a bug, since it's expected React dev
   behavior, but worth confirming with a production build if it comes up
   again.

## Known Gaps (deferred, not blockers for Step 2 completion)

- Only a single level of destination-string fallback (see Known Issue 1)
- No automated test specifically reproduces the exact real-world failure
  mode found (a live geocoding call returning the true "no `results`
  key" shape) — the new tests mock that shape based on what was observed
  via curl, which is a reasonable substitute but not a live-API
  integration test
- Weather feature has not been tested with a second concurrent user

## Conclusion

Step 2 (weather forecast extension) is complete and verified against
real external services, not just mocks. The loop mechanic demonstrated
its value twice in this step: once building the feature cleanly against
its own design doc, and again correcting a genuine bug that only
surfaced under real-world conditions the original tests didn't cover.
