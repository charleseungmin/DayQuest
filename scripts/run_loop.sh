#!/usr/bin/env bash
set -euo pipefail

# ==========================================
# configuration section
# ==========================================
TASK_FILE="docs/exec-plans/current-task.md"
STATE_DIR=".codex/state"
VERIFY_SCRIPT="scripts/verify.sh"
DONE_SCRIPT="scripts/check_done.sh"
STALL_SCRIPT="scripts/detect_stall.sh"
LOG_SCRIPT="scripts/log_iteration.sh"

# ==========================================
# state initialization
# ==========================================
mkdir -p "$STATE_DIR"
iteration=0
replan_requested="NO"
last_failure_signature="none"

printf '%s' "UNKNOWN" > "${STATE_DIR}/verify.status"
printf '%s' "UNKNOWN" > "${STATE_DIR}/reviewer.status"
printf '%s' "FAIL" > "${STATE_DIR}/done.status"
printf '%s' "PASS" > "${STATE_DIR}/current_task.status"

run_planner() {
  # planner call placeholder
  # 여기에 Codex CLI planner 호출 삽입
  echo "[loop] planner 실행 (placeholder)"
}

run_implementer() {
  # implementer call placeholder
  # 여기에 Codex CLI implementer 호출 삽입
  echo "[loop] implementer 실행 (placeholder)"
}

run_reviewer() {
  # reviewer call placeholder
  # 여기에 Codex CLI reviewer 호출 삽입
  # reviewer 결과는 PASS/FAIL 중 하나를 stdout으로 반환해야 한다.
  echo "FAIL"
}

# ==========================================
# infinite loop
# ==========================================
while true; do
  iteration=$((iteration + 1))
  planner_action="standard-planning"
  if [[ "$replan_requested" == "YES" ]]; then
    planner_action="replan-after-stall"
  fi

  # planner call
  run_planner

  # implementer call
  run_implementer

  verify_result="PASS"
  if "$VERIFY_SCRIPT"; then
    printf '%s' "PASS" > "${STATE_DIR}/verify.status"
    verify_result="PASS"
  else
    printf '%s' "FAIL" > "${STATE_DIR}/verify.status"
    verify_result="FAIL"
    last_failure_signature="verify-fail"
  fi

  # reviewer call
  reviewer_result="$(run_reviewer | tail -n1 | tr -d '[:space:]')"
  if [[ "$reviewer_result" != "PASS" ]]; then
    reviewer_result="FAIL"
    last_failure_signature="reviewer-fail"
  fi
  printf '%s' "$reviewer_result" > "${STATE_DIR}/reviewer.status"

  # done check
  if "$DONE_SCRIPT"; then
    printf '%s' "PASS" > "${STATE_DIR}/done.status"
  else
    printf '%s' "FAIL" > "${STATE_DIR}/done.status"
  fi

  # stall detection
  stall_detected="NO"
  if "$STALL_SCRIPT"; then
    stall_detected="YES"
    replan_requested="YES"
  else
    replan_requested="NO"
  fi

  # replan handling
  if [[ "$stall_detected" == "YES" ]]; then
    planner_action="replan-triggered-by-stall"
  fi

  # logging
  "$LOG_SCRIPT" \
    --iteration "$iteration" \
    --planner-action "$planner_action" \
    --implementer-action "implemented-by-placeholder" \
    --verify-result "$verify_result" \
    --reviewer-result "$reviewer_result" \
    --failure-signature "$last_failure_signature" \
    --stall-detected "$stall_detected" \
    --replan-triggered "$replan_requested" \
    --blocker "none" \
    --next-action "continue-loop"

  # exit handling
  if [[ "$(cat "${STATE_DIR}/done.status")" == "PASS" ]]; then
    echo "[loop] done-criteria 충족. 루프 종료"
    exit 0
  fi

  echo "[loop] 미완료. 다음 반복으로 진행 (iteration=${iteration})"
done
