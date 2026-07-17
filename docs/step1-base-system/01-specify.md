# Step 1 — Base System: Specify

Save this file at: `docs/step1-base-system/01-specify.md`

## Overview

A web application that lets a logged-in user plan a single camping trip. The
UI reveals itself progressively: a minimal search screen first, then a
detail screen once the trip is created. The user can pack for the trip by
checking off items on a checklist, and can wrap up (delete) the trip when
done.

Only one active trip exists per user at a time.

## User Flow

1. User logs in (or registers, if no account exists).
2. User sees Screen 1: a minimal search bar with **From**, **Where**, and
   **When** (start/end date), and a **Go** button.
3. User fills in From (starting location), Where (campsite/destination),
   and a date range, then clicks **Go**.
4. Backend creates the trip and seeds a default checklist.
5. User sees Screen 2:
   - A **Wrap Up** button at the top.
   - An embedded Google Map showing the driving route from "From" to
     "Where" (Google Maps Embed API, `directions` mode).
   - A countdown showing days remaining until the trip's start date.
   - A checklist of camping items, each with a checkbox.
6. User checks/unchecks checklist items; each change is saved to the
   database immediately.
7. User can click **Wrap Up** at any time to delete the trip and its
   checklist, returning them to Screen 1.

## Functional Requirements

### Authentication

- FR1: A user must register (username + password) before first use.
- FR2: A user must log in before accessing Screen 1 or Screen 2.
- FR3: Passwords are stored hashed, never in plain text.

### Trip Creation (Screen 1)

- FR4: User provides From (text), Where (text), start date, end date.
- FR5: Clicking Go creates a trip associated with the logged-in user.
- FR6: Creating a trip auto-generates a default checklist (see Checklist
  Defaults below).
- FR7: If the user already has an active trip, Screen 1 is skipped and
  Screen 2 is shown directly on login.

### Trip Detail (Screen 2)

- FR8: Display an embedded map showing the driving route from the trip's
  "From" to "Where" values.
- FR9: Display a countdown in days from today to the trip's start date.
- FR10: Display all checklist items with their current checked state.
- FR11: Toggling a checklist item persists the new state to the database
  immediately (no separate "save" step).
- FR12: Clicking Wrap Up deletes the trip and all its checklist items, and
  returns the user to Screen 1.

### Checklist Defaults

- FR13: When a trip is created, seed it with a standard camping checklist
  (author your own list — grouped into categories such as Shelter &
  Sleeping, Cooking & Food, Clothing, Tools & Safety; do not copy another
  site's list verbatim).

## Non-Functional Requirements

- NFR1: Runs fully via `docker compose up` (frontend, backend, MySQL).
- NFR2: Google Maps API key is supplied via a git-ignored `.env` file, with
  a committed `.env.example` showing the expected variable name.
- NFR3: No trip data is visible to a user other than its owner.

## Out of Scope for Step 1

- Multiple simultaneous trips per user
- Weather forecast (Step 2)
- Editing checklist items (adding/removing custom items)
- Password reset / email verification

## Data Model (draft)

- `users`: id, username, password_hash
- `trips`: id, user_id, origin, destination, start_date, end_date
- `checklist_items`: id, trip_id, item_name, category, is_packed

## Open Questions for Design Stage

- Exact default checklist content and categories
- Session/auth mechanism (JWT vs. server session)
- Whether "From"/"Where" need validation against real place names before
  hitting the map embed
