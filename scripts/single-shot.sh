#!/bin/bash
# Single-shot (non-looped) agent invocation — for small, well-defined tasks
# that don't need iterative build-verify-repeat correction, while still
# logging the prompt to prompts.txt per the assignment's requirement.
# Usage: ./scripts/single-shot.sh "<prompt>"

set -uo pipefail

# Anchor to repo root regardless of where this script is invoked from.
cd "$(dirname "$0")/.."

PROMPTS_FILE="prompts.txt"
prompt="$1"

{
  echo "### Single-shot task ($(date))"
  echo '```'
  echo "$prompt"
  echo '```'
  echo ""
} >> "$PROMPTS_FILE"

claude -p "$prompt" \
  --allowedTools "Read,Edit,Write,Bash" \
  --permission-mode acceptEdits \
  --max-turns 15 \
  --max-budget-usd 2.00 \
  --output-format json | jq -r '.result // "(no result text returned)"'
