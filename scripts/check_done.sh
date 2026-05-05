#!/usr/bin/env bash
set -euo pipefail

# 종료 가능 여부 판정:
# 1) verify 성공 상태
# 2) reviewer pass 상태
# 3) done-criteria 충족 상태
# 4) current-task 최신화 상태

STATE_DIR=".codex/state"
VERIFY_FILE="${STATE_DIR}/verify.status"
REVIEW_FILE="${STATE_DIR}/reviewer.status"
DONE_FILE="${STATE_DIR}/done.status"
TASK_FILE="docs/exec-plans/current-task.md"
TASK_SYNC_FILE="${STATE_DIR}/current_task.status"

fail=false

check_file_equals() {
  local file="$1"
  local expected="$2"
  local label="$3"

  if [[ ! -f "$file" ]]; then
    echo "[done-check] FAIL ${label}: 상태 파일 없음 (${file})" >&2
    fail=true
    return
  fi

  local actual
  actual="$(tr -d '[:space:]' < "$file")"
  if [[ "$actual" != "$expected" ]]; then
    echo "[done-check] FAIL ${label}: expected=${expected}, actual=${actual}" >&2
    fail=true
  else
    echo "[done-check] PASS ${label}"
  fi
}

check_file_equals "$VERIFY_FILE" "PASS" "verify"
check_file_equals "$REVIEW_FILE" "PASS" "reviewer"
check_file_equals "$DONE_FILE" "PASS" "done-criteria"
check_file_equals "$TASK_SYNC_FILE" "PASS" "current-task-sync"

if [[ ! -f "$TASK_FILE" ]]; then
  echo "[done-check] FAIL current-task: 파일 없음 (${TASK_FILE})" >&2
  fail=true
else
  if ! grep -q "## Loop history" "$TASK_FILE"; then
    echo "[done-check] FAIL current-task: Loop history 섹션 누락" >&2
    fail=true
  else
    echo "[done-check] PASS current-task: Loop history 확인"
  fi
fi

if [[ "$fail" == true ]]; then
  echo "[done-check] NOT DONE" >&2
  exit 1
fi

echo "[done-check] DONE"
