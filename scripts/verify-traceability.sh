#!/usr/bin/env bash
set -euo pipefail

mode="${1:-}"
if [[ "$mode" != "--staged" ]]; then
  echo "usage: $0 --staged" >&2
  exit 2
fi

mapfile_cmd="mapfile"
if ! command -v "$mapfile_cmd" >/dev/null 2>&1; then
  changed="$(git diff --cached --name-only --diff-filter=ACMR)"
  journal_count="$(printf '%s\n' "$changed" | awk '/^journal\/.*\.md$/ { count++ } END { print count+0 }')"
else
  mapfile -t files < <(git diff --cached --name-only --diff-filter=ACMR)
  journal_count=0
  for file in "${files[@]}"; do [[ "$file" == journal/*.md ]] && ((journal_count+=1)); done
fi

if [[ "$journal_count" -ne 1 ]]; then
  echo "expected exactly one staged journal file, found $journal_count" >&2
  exit 1
fi
echo "traceability gate passed"
