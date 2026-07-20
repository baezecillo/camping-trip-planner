# Step 1 — Base System: Review

Save this file at: `docs/step1-base-system/04-review.md`
(This replaces the earlier backend-only version of this file.)

## Verification Summary

Step 1 was verified in three layers, from narrowest to broadest:

1. **Automated (backend):** `scripts/build-loop.sh` — `./gradlew clean
   build test`, 7/7 tests passing against H2. Caught and self-corrected a
   real compile error (non-effectively-final variable in a lambda) across
   iterations 1→2.

2. **Automated (frontend):** `scripts/frontend-build-loop.sh` —
   `npm install && npm test -- --run && npm run build`, all passing.
   Caught and self-corrected a real gap (no `test` script defined, so
   required test files hadn't been wired up) across iterations 1→2.

3. **Manual, full stack, real services:**
   - Backend manually exercised via `curl` against real MySQL through
     `docker compose up --build` (register → login → 404-when-empty →
     create trip with 17-item seeded checklist → toggle persists → wrap
     up cascades → back to 404).
   - CORS gap identified when testing from an actual browser (curl isn't
     subject to CORS, so this wasn't caught until real frontend↔backend
     traffic was tested) — fixed via a single-shot task
     (`scripts/single-shot.sh`, not the iterative loop, since it was a
     small well-defined config change) — see Known Issues below.
   - Full browser walkthrough completed: registered a user, submitted
     the Where/When/From search, transitioned correctly to the trip
     detail screen, embedded Google Map rendered the driving route,
     countdown displayed, checklist rendered grouped by category, and
     the backend + MySQL confirmed to be receiving and persisting
     requests throughout.

This three-layer approach mattered in practice: the automated loops
caught code-level bugs, but the CORS gap only surfaced once real browser
traffic was involved — automated tests and curl alone would not have
caught it, since neither is subject to a browser's CORS enforcement.

## Requirements Traceability (against 01-specify.md)

All FR1–FR13 and NFR1–NFR3 are now satisfied end-to-end, frontend
included:

- FR1–FR3 (auth): verified via curl and live browser registration/login.
- FR4–FR7, FR13 (trip creation + checklist seed): verified, 17 items
  across 4 categories, confirmed both via curl and rendered in-browser.
- FR8–FR12 (trip detail + persistence): map, countdown, and checklist
  all confirmed rendering and functioning in a real browser session.
- NFR1 (`docker compose up`): verified.
- NFR2 (git-ignored `.env`, committed `.env.example`, both root and
  frontend): in place.
- NFR3 (no cross-user data leakage): covered by automated test; not
  re-verified manually with a second concurrent user.

## Known Issues / Discrepancies Found During the Loop

1. **Checklist count inconsistency in the design doc** — found and fixed
   during the backend loop (see prior review notes; doc now says 17,
   matching the table).

2. **`application.yaml` credential handling is inconsistent** — datasource
   URL/db name hardcoded, credentials parameterized. Non-blocking,
   documented as a future cleanup item.

3. **Flyway/MySQL version mismatch warning** — MySQL 8.4 exceeds Flyway's
   officially tested range (8.1). Ran successfully regardless.

4. **CORS was not part of the original backend task scope**, and was only
   discovered once real browser traffic was tested — curl-based
   verification does not exercise CORS at all, since CORS is a
   browser-enforced mechanism, not a server-side concept the server
   itself refuses requests over. Fixed via a single-shot (non-looped)
   task: added a `CorsConfigurationSource` scoped to `/api/**`, explicitly
   avoiding a wildcard origin (incompatible with `allowCredentials(true)`
   per the CORS spec), and wired it into the Spring Security filter chain
   directly rather than leaving it as a standalone, unreferenced bean.

5. **Google Maps Embed API key setup** — initial "API key is invalid"
   error in-browser was not a code bug; it was simply that no real key
   had been provisioned yet. Resolved by creating a Google Cloud project,
   enabling the Maps Embed API specifically (not the JS SDK), and adding
   a referrer-restricted key to `frontend/.env`.

## Known Gaps (deferred, not blockers for Step 1 completion)

- NFR3 (cross-user isolation) verified only via automated test, not a
  manual two-user browser/curl pass.
- No CI pipeline runs either build-loop script automatically; both were
  run manually for this stage.
- Production API key restrictions (HTTP referrer) are scoped to
  `localhost:5173` for local dev only; would need updating for any real
  deployment.

## Conclusion

Step 1 (base system) is complete: backend and frontend both implemented,
integrated, and verified against real services (MySQL, a real browser)
rather than mocks or in-memory substitutes alone. The build loop
(backend and frontend) is documented in `03-build.md`, with full raw
evidence in `prompts.txt` and `build-loop-raw.jsonl`.
