#!/bin/bash
# Build-verify-repeat loop for the React/Vite frontend.
# Usage: ./scripts/frontend-build-loop.sh "<initial task prompt>" [doc-dir]
#   doc-dir defaults to docs/step1-base-system for backward compatibility.

set -uo pipefail

# Anchor to repo root regardless of where this script is invoked from.
cd "$(dirname "$0")/.."

MAX_ITERATIONS=5
MAX_TURNS_PER_CALL=30
MAX_BUDGET_USD_PER_CALL=3.00
TASK_PROMPT="$1"
DOC_DIR="${2:-docs/step1-base-system}"
FRONTEND_DIR="frontend"
LOG_FILE="$DOC_DIR/03-build.md"
PROMPTS_FILE="prompts.txt"
RAW_LOG="$DOC_DIR/build-loop-raw.jsonl"

if ! command -v jq &> /dev/null; then
  echo "jq is required to parse Claude Code's JSON output. Install it with:"
  echo "  sudo apt install -y jq"
  exit 1
fi

iteration=1
feedback=""
total_cost="0"

echo "=== Frontend build loop started: $(date) ===" >> "$LOG_FILE"

while [ "$iteration" -le "$MAX_ITERATIONS" ]; do
  echo ""
  echo "--- Frontend Iteration $iteration ---"

  if [ "$iteration" -eq 1 ]; then
    prompt="$TASK_PROMPT"
  else
    prompt="$TASK_PROMPT

The previous attempt failed with the following output. Fix the issue(s)
and keep the rest of the implementation intact:

$feedback"
  fi

  {
    echo "### Frontend Iteration $iteration ($(date))"
    echo '```'
    echo "$prompt"
    echo '```'
    echo ""
  } >> "$PROMPTS_FILE"

  claude_json=$(claude -p "$prompt" \
    --allowedTools "Read,Edit,Write,Bash" \
    --permission-mode acceptEdits \
    --max-turns "$MAX_TURNS_PER_CALL" \
    --max-budget-usd "$MAX_BUDGET_USD_PER_CALL" \
    --output-format json)

  echo "$claude_json" | jq -r '.result // "(no result text returned)"'

  echo "$claude_json" | jq -c --arg iter "$iteration" --arg ts "$(date -Iseconds)" \
    '. + {iteration: ($iter | tonumber), stage: "frontend", logged_at: $ts}' >> "$RAW_LOG"

  iter_cost=$(echo "$claude_json" | jq -r '.total_cost_usd // 0')
  iter_turns=$(echo "$claude_json" | jq -r '.num_turns // "unknown"')
  is_error=$(echo "$claude_json" | jq -r '.is_error // false')
  total_cost=$(echo "$total_cost + $iter_cost" | bc)

  {
    echo "**Frontend Iteration $iteration agent call:** turns=$iter_turns, cost=\$${iter_cost}, is_error=${is_error}"
  } >> "$LOG_FILE"

  if [ "$is_error" = "true" ]; then
    echo "⚠️  Claude Code reported an error for frontend iteration $iteration (see $LOG_FILE / prompts.txt)" | tee -a "$LOG_FILE"
  fi

  # VERIFY — install deps, run tests, then a production build.
  # Chained with && so the exit code reflects the first failing step.
  build_output=$(cd "$FRONTEND_DIR" && npm install 2>&1 && CI=true npm test -- --run 2>&1 && npm run build 2>&1)
  exit_code=$?

  echo "Exit code: $exit_code" >> "$LOG_FILE"

  if [ "$exit_code" -eq 0 ]; then
    echo "✅ Frontend install + tests + build passed on iteration $iteration" | tee -a "$LOG_FILE"
    echo "**Total frontend loop cost: \$${total_cost}**" | tee -a "$LOG_FILE"
    exit 0
  fi

  echo "❌ Frontend iteration $iteration failed" | tee -a "$LOG_FILE"
  feedback="$build_output"
  iteration=$((iteration + 1))
done

echo "🛑 Max iterations ($MAX_ITERATIONS) reached without a passing frontend build." | tee -a "$LOG_FILE"
echo "**Total frontend loop cost: \$${total_cost}**" | tee -a "$LOG_FILE"
echo "Manual intervention required — see $LOG_FILE for the last failure."
exit 1