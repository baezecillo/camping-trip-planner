# Step 2 — Extension: Build (Loop Design)

Save this file at: `docs/step2-extension/03-build.md`

## Loop

Reuses the same build-verify-repeat loop mechanic documented in
`docs/step1-base-system/03-build.md` (trigger → act → verify → feed back
→ repeat, 5-iteration safety cap, 30-turn/$3.00 per-call guardrails).
Not re-explained here in full — see that file for the loop's design
rationale.

What's different for Step 2: the driving scripts (`scripts/build-loop.sh`,
`scripts/frontend-build-loop.sh`) now accept a second argument for which
doc directory to log into, so Step 2's run log lands here instead of
overwriting Step 1's:

```bash
./scripts/build-loop.sh "$(cat prompts/step2-extension/01-build-backend-initial-task.txt)" docs/step2-extension
```

## Run Log

_(Append here after running the backend loop for Step 2 — iteration
count, final result, notes on what the agent fixed each pass.)_
