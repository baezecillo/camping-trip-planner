# Step 1 — Base System: Build (Loop Design)

Save this file at: `docs/step1-base-system/03-build.md`
(Append the actual run log to the bottom of this file once the loop has
been executed — see "Run Log" section at the end.)

## Loop Skeleton

The Build stage for the backend is driven by an automated loop, not manual
back-and-forth prompting. The loop's job: keep handing the agent
implementation work + the latest failure output, until the backend
actually builds and passes its tests, or a safety cap is hit.

```text
┌──────────────────────────────────────────────┐
│  1. TRIGGER                                  │
│     Iteration 1: initial implementation task │
│     Iteration N: previous iteration's        │
│                  build/test failure output   │
└───────────────────┬──────────────────────────┘
                     ▼
┌──────────────────────────────────────────────┐
│  2. ACT (agent)                              │
│     claude -p "<task + prior errors>"        │
│     --allowedTools "Read,Edit,Write,Bash"    │
│     --permission-mode acceptEdits            │
└───────────────────┬──────────────────────────┘
                     ▼
┌──────────────────────────────────────────────┐
│  3. VERIFY (script, not the agent)           │
│     ./gradlew build test                     │
│     capture exit code + full output          │
└───────────────────┬──────────────────────────┘
                     ▼
              exit code == 0?
              /            \
            yes              no
             │                │
             ▼                ▼
   ┌─────────────────┐   ┌────────────────────-─────┐
   │ 4. STOP         │   │ 5. FEED BACK             │
   │ log success,    │   │ append failure output to │
   │ exit loop       │   │ next prompt, increment   │
   │                 │   │ iteration count, go to 2 │
   └─────────────────┘   └────────────┬─────────────┘
                                      │
                          iteration count > MAX (5)?
                                      │
                                     yes → STOP, flag for
                                           manual intervention
```

### Key properties

- **Trigger:** first call is the task description; every call after that
  is the task description **plus** the exact build/test failure output
  from the previous iteration. The agent never re-guesses what's wrong —
  it's handed the real error.
- **Verification is external to the agent.** The agent doesn't get to
  decide "I think this works now" — `./gradlew build test` is run by the
  driving script, and only a `0` exit code counts as success. This is the
  actual "loop" mechanic: act → observe (real, tool-generated
  observation) → decide → repeat.
- **Stop condition:** `./gradlew build test` exits `0`.
- **Safety cap:** 5 iterations max. If not resolved by then, the loop
  halts and the failure is handled manually — an infinite/runaway loop is
  a bug, not a feature, of this design.
- **Every iteration is logged** (prompt sent, exit code, summary of
  output) to `docs/step1-base-system/03-build.md` under Run Log, and the
  raw prompts also get appended to `prompts.txt` at the repo root per the
  assignment's requirement.

## Driving Script

Saved at: `scripts/build-loop.sh`

## How This Maps to the Assignment's Loop Requirement

The professor's example was "a loop for plan and build." This script
implements the **build** half directly: iteration count, feedback
mechanism, and stop condition are all explicit and script-enforced, not
just "I re-prompted a few times until it worked." The prompt log
(`prompts.txt`) plus this file's Run Log together document exactly what
the loop did on each pass.

## Run Log

_(Append here after running `scripts/build-loop.sh` — iteration count,
final result, and a short note on what the agent fixed each pass.)
