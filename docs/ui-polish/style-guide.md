# UI Style Guide — camping-trip-planner

Save this file at: `docs/ui-polish/style-guide.md`

This exists so "make it look nice" becomes something an agent can
actually implement precisely, instead of guessing. Direction: earthy /
outdoorsy, loosely inspired by REI / Patagonia (rugged, photography-
forward, confident), spacious layout with generous whitespace and larger
touch targets.

## Color Palette

| Token                    | Hex       | Use                                                            |
| ------------------------ | --------- | -------------------------------------------------------------- |
| `--color-forest`         | `#2D4A34` | Primary — headers, primary buttons, nav                        |
| `--color-forest-dark`    | `#1F3524` | Primary button hover/active state                              |
| `--color-trail-brown`    | `#8B5E34` | Accent — secondary buttons, highlights, active checklist items |
| `--color-cream`          | `#F7F3EC` | Page background                                                |
| `--color-card`           | `#FFFFFF` | Card/panel background (slightly lifted off the cream page bg)  |
| `--color-charcoal`       | `#2A2A24` | Primary text                                                   |
| `--color-charcoal-muted` | `#6B6B5F` | Secondary/muted text (labels, captions)                        |
| `--color-border`         | `#E0DACB` | Card borders, dividers                                         |
| `--color-success`        | `#4A7A4E` | Confirmations (e.g. checklist item packed)                     |
| `--color-error`          | `#A6402F` | Error/validation states                                        |

## Typography

- **Headings:** "Barlow Condensed", bold (700) — condensed, confident,
  trail-sign-like feel without going full rustic/decorative (keeps it
  legible and modern-outdoorsy rather than kitschy).
- **Body text:** "Source Sans 3", regular (400) / semibold (600) for
  emphasis — clean and highly readable, since this is a utility app
  people will actually read carefully (checklists, dates).
- Both available free via Google Fonts.
- Base body size: 16px. Heading scale: h1 32px / h2 24px / h3 20px,
  all using the condensed heading font.

## Spacing & Layout (spacious, per direction)

- Base spacing unit: 8px, using multiples (8/16/24/32/48px) rather than
  arbitrary values — keeps rhythm consistent across components.
- Card padding: 24px minimum.
- Section/page margins: 32–48px.
- Minimum touch target height: 44px (buttons, checklist checkboxes,
  inputs) — generous, not cramped.

## Shape & Elevation

- Border radius: 8px on inputs/buttons, 12px on cards — rounded enough
  to feel approachable, not so rounded it feels playful/childish.
- Shadows: soft and subtle only — `0 2px 8px rgba(42, 42, 36, 0.08)` on
  cards. Avoid heavy/dark shadows; keep the rugged feel grounded rather
  than flashy.
- Buttons: solid fill (forest green primary, trail-brown secondary),
  no gradients — REI/Patagonia's confidence comes from restraint, not
  decoration.

## Optional (nice-to-have, not required)

A subtle topographic-line pattern (very low-opacity, in
`--color-border` tone) as a background texture on the search screen only
— evokes the "photography-forward, outdoorsy" reference without needing
actual photography (which would need licensed/sourced images, out of
scope for a course project). Skip entirely if it adds meaningful
complexity — the palette and typography alone will carry most of the
mood change.

## What NOT to change

This is a visual-only pass. Do not alter component logic, API calls,
state management, routing, or existing test assertions about behavior
(only update test assertions that specifically check for old class
names/styles, if any exist).
