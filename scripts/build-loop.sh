#!/bin/bash
# Build-verify-repeat loop for the Spring Boot backend.
# Usage: ./scripts/build-loop.sh "<initial task prompt>"

set -uo pipefail

# Anchor to repo root regardless of where this script is invoked from.
cd "$(dirname "$0")/.."

MAX_ITERATIONS=5
TASK_PROMPT="$1"
BACKEND_DIR="backend"
LOG_FILE="docs/step1-base-system/03-build.md"
PROMPTS_FILE="prompts.txt"

iteration=1
feedback=""

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

  # 2. ACT — invoke the agent non-interactively
  claude -p "$prompt" \
    --allowedTools "Read,Edit,Write,Bash" \
    --permission-mode acceptEdits

  # 3. VERIFY — external, ground-truth check
  build_output=$(cd "$BACKEND_DIR" && ./gradlew build test 2>&1)
  exit_code=$?

  echo "Exit code: $exit_code" >> "$LOG_FILE"

  if [ "$exit_code" -eq 0 ]; then
    echo "✅ Build + tests passed on iteration $iteration" | tee -a "$LOG_FILE"
    exit 0
  fi

  echo "❌ Iteration $iteration failed" | tee -a "$LOG_FILE"
  feedback="$build_output"
  iteration=$((iteration + 1))
done

echo "🛑 Max iterations ($MAX_ITERATIONS) reached without a passing build." | tee -a "$LOG_FILE"
echo "Manual intervention required — see $LOG_FILE for the last failure."
exit 1