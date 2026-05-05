# DayQuest Codex 작업 시작 규칙

## 프로젝트 목적
- 외부 루프 기반 Codex 실행 구조(planner/implementer/reviewer)를 표준화한다.
- done-criteria 충족 전까지 반복하고, 정체(stall) 시 planner 재계획으로 전환한다.

## 먼저 읽을 문서
1. `docs/exec-plans/current-task.md`
2. `docs/loop/loop-policy.md`
3. `docs/review-rules/done-criteria.md`
4. `docs/review-rules/pr-checklist.md`
5. `docs/runbooks/build-and-test.md`
6. `docs/architecture/overview.md`
7. `docs/domain/core-rules.md`

## 역할 요약
- planner: 계획/범위/우선순위/대안 전략 수립, stall 시 재계획.
- implementer: 코드·문서 변경, verify 준비 및 실행 결과 반영.
- reviewer: 체크리스트와 done-criteria 기준으로 pass/fail 판정.

## 외부 루프 사용 원칙
- 루프 제어는 Codex 내부가 아니라 `scripts/run_loop.sh`가 담당한다.
- Codex는 planner/implementer/reviewer 실제 작업만 수행한다.
- stall 감지는 `scripts/detect_stall.sh`가 담당한다.
- 종료 판정은 `scripts/check_done.sh`만 수행한다.

## 필수 행동 규칙
- 작업 시작 전 `docs/exec-plans/current-task.md`를 읽는다.
- 작업 시작 전 관련 docs를 확인한다.
- 구현 완료 선언 전 `scripts/verify.sh`를 실행한다.
- reviewer fail 상태에서는 완료 선언 금지.
- done-criteria 미충족 상태에서는 완료 선언 금지.
- 동일 실패 반복 시 종료하지 말고 planner 재계획으로 전환한다.
- loop 기록은 `scripts/log_iteration.sh`로 남긴다.

## 검증 및 종료 규칙
- 기계 검증 게이트는 `scripts/verify.sh` 단일 기준으로 사용한다.
- success 종료는 `scripts/check_done.sh`가 0을 반환할 때만 허용한다.
- verify FAIL 또는 reviewer FAIL이면 loop는 계속 수행한다.
- stall 감지 시 종료하지 않고 planner 재개입을 강제한다.

## 문서 갱신 규칙
- 코드 변경 시 관련 docs 갱신 필요 여부를 같은 반복에서 판정한다.
- `current-task.md`는 반복 결과, blocker, 재계획 이유를 최신 상태로 유지한다.
- 상세 정책은 `docs/`를 기준으로 한다.
