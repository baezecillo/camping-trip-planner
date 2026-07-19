# Step 1 — Base System: Review

Save this file at: `docs/step1-base-system/04-review.md`

## Verification Summary

The base system backend was verified two ways:

1. **Automated:** `scripts/build-loop.sh`, iteration 2 — `./gradlew clean
   build test` passing 7/7 tests (register/login, checklist seeding,
   duplicate-trip 409, checklist toggle, wrap-up cascade, cross-user
   403/404 isolation). Tests ran against an in-memory H2 database.

2. **Manual, against real MySQL via `docker compose up --build`:**
   Full lifecycle exercised with `curl` against the live container stack.

   | Step         | Endpoint                    | Result                                                                                                  |
   | ------------ | --------------------------- | ------------------------------------------------------------------------------------------------------- |
   | Register     | `POST /api/auth/register`   | 201, user created                                                                                       |
   | Login        | `POST /api/auth/login`      | 200, session cookie issued                                                                              |
   | No trip yet  | `GET /api/trips/current`    | 404, correct message                                                                                    |
   | Create trip  | `POST /api/trips`           | 201, all 17 checklist items seeded across the correct 4 categories, `daysUntilStart` computed correctly |
   | Toggle item  | `PATCH /api/checklist/{id}` | 200, and confirmed via a follow-up GET that the change persisted (not just echoed)                      |
   | Wrap Up      | `DELETE /api/trips/current` | 204                                                                                                     |
   | Post-wrap-up | `GET /api/trips/current`    | 404 again — confirms cascade delete removed the trip and its checklist items cleanly                    |

This second pass matters specifically because the automated test suite
only exercised H2, not MySQL. Running the real stack surfaced that Flyway
migrations and JPA mappings are valid against actual MySQL 8.4, not just
H2's more lenient SQL dialect.

## Requirements Traceability (against 01-specify.md)

All FR1–FR13 and NFR1–NFR3 from the specify doc are satisfied:

- Auth (FR1–FR3): verified via register/login flow above.
- Trip creation + checklist seed (FR4–FR7, FR13): verified, 17 items
  across 4 categories.
- Trip detail + persistence (FR8–FR12): map/countdown are frontend
  concerns (Step 1 frontend still pending — see Known Gaps); checklist
  persistence and Wrap Up cascade verified above.
- NFR1 (`docker compose up`): verified.
- NFR2 (git-ignored `.env`, committed `.env.example`): in place.
- NFR3 (no cross-user data leakage): covered by the automated test suite's
  403/404 isolation test; not manually re-verified with a second user in
  this pass.

## Known Issues / Discrepancies Found During the Loop

1. **Checklist count inconsistency in the design doc (fixed).**
   `02-design.md` originally said "16 default items" while its own table
   listed 17 (4+5+4+4). The build-loop agent caught this while writing
   tests, correctly treated the table as authoritative, and built against
   17. The prose was corrected after the fact to match.

2. **`application.yaml` credential handling is inconsistent.** Datasource
   URL and database name (`campingtrip`) are hardcoded rather than
   templated from environment variables, while username/password *are*
   parameterized (`${DB_USERNAME}` / `${DB_PASSWORD}`) — an unusual
   half-and-half approach. Not a functional bug (`docker-compose.yml` was
   adjusted to match these exact variable names), but worth normalizing
   later: either template all four values, or hardcode all four for a
   single-environment course project. Left as-is for Step 1 to avoid
   re-triggering the build loop for a non-functional cleanup.

3. **Flyway/MySQL version mismatch warning.** Flyway logged that MySQL 8.4
   is newer than its officially tested range (latest verified: 8.1).
   Migrations ran successfully regardless; flagged here in case a future
   MySQL image bump introduces a real incompatibility.

4. **Iteration 1 of the build loop hit `--max-turns`,** producing no
   result text and an `is_error: true` log entry. Root cause: the initial
   task (bootstrap Gradle Wrapper + full backend + tests in one shot) was
   too large for the original 25-turn cap. Fixed by raising
   `MAX_TURNS_PER_CALL` to 30 and adding raw JSONL logging
   (`docs/step1-base-system/build-loop-raw.jsonl`) so a future truncation
   is fully diagnosable instead of just showing up as a blank result.

## Known Gaps (deferred, not blockers for Step 1 completion)

- Frontend (React) has not been built yet — Screen 1 and Screen 2 exist
  only as design artifacts so far, not code.
- NFR3 (cross-user isolation) verified only via automated test, not a
  manual two-user curl pass.
- No CI pipeline runs `build-loop.sh` automatically; it was run manually
  for this stage.

## Conclusion

The Step 1 **backend** is functionally complete and verified against a
real MySQL instance via Docker Compose. Frontend implementation is the
remaining piece before Step 1 as a whole (per the original specify doc)
is done.
