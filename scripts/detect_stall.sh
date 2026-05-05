#!/usr/bin/env bash
set -euo pipefail

# 최근 loop history를 기반으로 stall(정체) 여부를 감지한다.
# exit 0: stall 감지
# exit 1: stall 미감지

TASK_FILE="docs/exec-plans/current-task.md"
WINDOW="${STALL_WINDOW:-4}"

if [[ ! -f "$TASK_FILE" ]]; then
  echo "[stall] current-task 파일이 없어 stall 판단 불가"
  exit 1
fi

extract_recent_values() {
  local key="$1"
  awk -v key="$key" '
    $0 ~ "^- " key ":" {
      line=$0
      sub("^- " key ":[[:space:]]*", "", line)
      if (line != "" && line !~ /:$/) print line
    }
  ' "$TASK_FILE" | tail -n "$WINDOW"
}

all_same_nonempty() {
  local data="$1"
  local count
  count="$(echo "$data" | sed '/^$/d' | wc -l | tr -d ' ')"
  [[ "$count" -lt 2 ]] && return 1

  local uniq_count
  uniq_count="$(echo "$data" | sed '/^$/d' | sort -u | wc -l | tr -d ' ')"
  [[ "$uniq_count" -eq 1 ]]
}

verify_list="$(extract_recent_values "Verify result")"
reviewer_list="$(extract_recent_values "Reviewer result")"
signature_list="$(extract_recent_values "Failure signature")"
blocker_list="$(extract_recent_values "Blocker")"
next_list="$(extract_recent_values "Next action")"

if all_same_nonempty "$signature_list" && [[ "$(echo "$signature_list" | tail -n1)" != "none" ]]; then
  echo "[stall] detected: repeated failure signature ($(echo "$signature_list" | tail -n1))"
  exit 0
fi

if all_same_nonempty "$reviewer_list" && [[ "$(echo "$reviewer_list" | tail -n1)" == "FAIL" ]]; then
  echo "[stall] detected: repeated reviewer FAIL"
  exit 0
fi

if all_same_nonempty "$verify_list" && [[ "$(echo "$verify_list" | tail -n1)" == "FAIL" ]]; then
  echo "[stall] detected: repeated verify FAIL"
  exit 0
fi

if all_same_nonempty "$blocker_list" && [[ "$(echo "$blocker_list" | tail -n1)" != "none" ]]; then
  echo "[stall] detected: repeated blocker ($(echo "$blocker_list" | tail -n1))"
  exit 0
fi

if all_same_nonempty "$next_list" && [[ -n "$(echo "$next_list" | tail -n1)" ]]; then
  echo "[stall] detected: next action not changing"
  exit 0
fi

echo "[stall] not detected"
exit 1
