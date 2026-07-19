#!/bin/bash
# Build-verify-repeat loop for the Spring Boot backend.
# Usage: ./scripts/build-loop.sh "<initial task prompt>"

set -uo pipefail

# Anchor to repo root regardless of where this script is invoked from.
cd "$(dirname "$0")/.."

MAX_ITERATIONS=5
MAX_TURNS_PER_CALL=30        # primary guardrail — relevant to Pro/Max subscriptions
MAX_BUDGET_USD_PER_CALL=3.00 # secondary guardrail — cost-equivalent ceiling, tracked either way
TASK_PROMPT="$1"
BACKEND_DIR="backend"
LOG_FILE="docs/step1-base-system/03-build.md"
PROMPTS_FILE="prompts.txt"
RAW_LOG="docs/step1-base-system/build-loop-raw.jsonl"

if ! command -v jq &> /dev/null; then
  echo "jq is required to parse Claude Code's JSON output. Install it with:"
  echo "  sudo apt install -y jq"
  exit 1
fi

iteration=1
feedback=""
total_cost="0"

echo "=== Build loop started: $(date) ===" >> "$LOG_FILE"

while [ "$iteration" -le "$MAX_ITERATIONS" ]; do
  echo ""
  echo "--- Iteration $iteration ---"

  if [ "$iteration" -eq 1 ]; then
    prompt="$TASK_PROMPT"
  else
    prompt="$TASK_PROMPT

The previous attempt failed with the following output. Fix the issue(s)
and keep the rest of the implementation intact:

$feedback"
  fi

  # Log the exact prompt sent, per the assignment's prompts.txt requirement
  {
    echo "### Iteration $iteration ($(date))"
    echo '```'
    echo "$prompt"
    echo '```'
    echo ""
  } >> "$PROMPTS_FILE"

  # 2. ACT — invoke the agent non-interactively, with turn/budget guardrails
  claude_json=$(claude -p "$prompt" \
    --allowedTools "Read,Edit,Write,Bash" \
    --permission-mode acceptEdits \
    --max-turns "$MAX_TURNS_PER_CALL" \
    --max-budget-usd "$MAX_BUDGET_USD_PER_CALL" \
    --output-format json)

  # Surface the agent's own summary to the terminal for visibility
  echo "$claude_json" | jq -r '.result // "(no result text returned)"'

  # Persist the full raw response for this iteration — one JSON object per
  # line, tagged with iteration number and timestamp. This is what makes a
  # cut-off/truncated call (like a max-turns hit) fully diagnosable later,
  # instead of just showing up as "(no result text returned)".
  echo "$claude_json" | jq -c --arg iter "$iteration" --arg ts "$(date -Iseconds)" \
    '. + {iteration: ($iter | tonumber), logged_at: $ts}' >> "$RAW_LOG"

  iter_cost=$(echo "$claude_json" | jq -r '.total_cost_usd // 0')
  iter_turns=$(echo "$claude_json" | jq -r '.num_turns // "unknown"')
  is_error=$(echo "$claude_json" | jq -r '.is_error // false')
  total_cost=$(echo "$total_cost + $iter_cost" | bc)

  {
    echo "**Iteration $iteration agent call:** turns=$iter_turns, cost=\$${iter_cost}, is_error=${is_error}"
  } >> "$LOG_FILE"

  if [ "$is_error" = "true" ]; then
    echo "⚠️  Claude Code reported an error for iteration $iteration (see $LOG_FILE / prompts.txt)" | tee -a "$LOG_FILE"
  fi

  # 3. VERIFY — external, ground-truth check
  build_output=$(cd "$BACKEND_DIR" && ./gradlew build test 2>&1)
  exit_code=$?

  echo "Exit code: $exit_code" >> "$LOG_FILE"

  if [ "$exit_code" -eq 0 ]; then
    echo "✅ Build + tests passed on iteration $iteration" | tee -a "$LOG_FILE"
    echo "**Total loop cost: \$${total_cost}**" | tee -a "$LOG_FILE"
    exit 0
  fi

  echo "❌ Iteration $iteration failed" | tee -a "$LOG_FILE"
  feedback="$build_output"
  iteration=$((iteration + 1))
done

echo "🛑 Max iterations ($MAX_ITERATIONS) reached without a passing build." | tee -a "$LOG_FILE"
echo "**Total loop cost: \$${total_cost}**" | tee -a "$LOG_FILE"
echo "Manual intervention required — see $LOG_FILE for the last failure."
exit 1