#!/usr/bin/env bash
set -euo pipefail

# 각 반복 상태를 current-task.md에 append 기록한다.
# 사용법:
# ./scripts/log_iteration.sh \
#   --iteration 3 \
#   --planner-action "재계획 반영" \
#   --implementer-action "verify 오류 수정" \
#   --verify-result "FAIL" \
#   --reviewer-result "FAIL" \
#   --failure-signature "gradle-wrapper-proxy-403" \
#   --stall-detected "YES" \
#   --replan-triggered "YES" \
#   --blocker "proxy" \
#   --next-action "network 확인 후 재실행"

TASK_FILE="docs/exec-plans/current-task.md"

iteration=""
planner_action=""
implementer_action=""
verify_result="UNKNOWN"
reviewer_result="UNKNOWN"
failure_signature="none"
stall_detected="NO"
replan_triggered="NO"
blocker="none"
next_action="update next step"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --iteration) iteration="$2"; shift 2 ;;
    --planner-action) planner_action="$2"; shift 2 ;;
    --implementer-action) implementer_action="$2"; shift 2 ;;
    --verify-result) verify_result="$2"; shift 2 ;;
    --reviewer-result) reviewer_result="$2"; shift 2 ;;
    --failure-signature) failure_signature="$2"; shift 2 ;;
    --stall-detected) stall_detected="$2"; shift 2 ;;
    --replan-triggered) replan_triggered="$2"; shift 2 ;;
    --blocker) blocker="$2"; shift 2 ;;
    --next-action) next_action="$2"; shift 2 ;;
    *) echo "[log-iteration] 알 수 없는 인자: $1" >&2; exit 2 ;;
  esac
done

if [[ -z "$iteration" ]]; then
  echo "[log-iteration] --iteration 값이 필요합니다." >&2
  exit 2
fi

mkdir -p .codex/state

cat >> "$TASK_FILE" <<EOF_ENTRY

### Iteration ${iteration}
- Iteration: ${iteration}
- Planner action: ${planner_action}
- Implementer action: ${implementer_action}
- Verify result: ${verify_result}
- Reviewer result: ${reviewer_result}
- Failure signature: ${failure_signature}
- Stall detected: ${stall_detected}
- Replan triggered: ${replan_triggered}
- Blocker: ${blocker}
- Next action: ${next_action}
- Timestamp (UTC): $(date -u +"%Y-%m-%dT%H:%M:%SZ")
EOF_ENTRY

printf '%s' "PASS" > .codex/state/current_task.status
echo "[log-iteration] appended iteration ${iteration}"
