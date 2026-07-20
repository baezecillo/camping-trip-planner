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
=== Build loop started: Mon Jul 20 12:46:19 EDT 2026 ===
**Iteration 1 agent call:** turns=30, cost=$0.8849425000000001, is_error=false
Exit code: 0
✅ Build + tests passed on iteration 1
**Total loop cost: $.8849425000000001**
=== Frontend build loop started: Mon Jul 20 13:09:13 EDT 2026 ===
**Frontend Iteration 1 agent call:** turns=25, cost=$0.4844947, is_error=false
Exit code: 0
✅ Frontend install + tests + build passed on iteration 1
**Total frontend loop cost: $.4844947**
=== Build loop started: Mon Jul 20 13:41:16 EDT 2026 ===
**Iteration 1 agent call:** turns=14, cost=$0.45033549999999994, is_error=false
Exit code: 0
✅ Build + tests passed on iteration 1
**Total loop cost: $.45033549999999994**
