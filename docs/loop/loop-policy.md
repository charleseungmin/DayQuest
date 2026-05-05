# External Loop Policy

## 외부 루프 목적
- done-criteria 충족 전까지 반복 실행한다.
- 동일 실패 반복을 감지하면 종료하지 않고 planner 재계획으로 전환한다.

## 역할 계약
### planner
- 입력: 요구사항, `current-task.md`, 관련 docs, stall/replan 이유
- 출력: 수정된 계획, 범위, 우선순위, 대안 전략, blocker 해석

### implementer
- 입력: planner 결과, docs, skills, reviewer 피드백, verify 실패 정보
- 출력: 코드/문서 변경, 변경 요약, verify 준비 상태

### reviewer
- 입력: 구현 결과, verify 결과, done-criteria, PR checklist
- 출력: pass/fail, 실패 이유, 수정 요구 항목, failure signature

## Standard Loop
1. `current-task.md` 읽기
2. planner 실행 또는 재계획 여부 판단
3. implementer 실행
4. `scripts/verify.sh` 실행
5. reviewer 실행
6. `scripts/check_done.sh` 실행
7. success 아니면 로그 기록 후 다음 반복
8. stall 감지 시 planner 재계획으로 전환
9. done-criteria 충족 시 종료

## 정체 감지 기준
- verify 실패 signature 반복
- reviewer failure signature 반복
- 변경 요약이 반복적으로 동일
- done 상태가 반복적으로 미충족
- blocker가 반복적으로 동일

## Replan Rule
- 동일 verify 실패 반복 시 planner 재개입
- 동일 reviewer FAIL 반복 시 planner 재개입
- 동일 blocker 반복 시 planner 재개입
- Next step이 진전을 만들지 못하면 planner 재개입
- 전략 변화가 없으면 planner 재개입

## Stall Policy
- stall은 종료 신호가 아니다.
- stall은 planner 재계획 트리거다.
- planner는 이전 반복과 다른 접근을 명시해야 한다.

## Success Rule
- verify PASS
- reviewer PASS
- done-criteria PASS
- current-task 최신화 완료
- 위 4개를 동시에 충족할 때만 종료

## 로그 기록 규칙
- 모든 반복은 `scripts/log_iteration.sh`로 append 기록
- 기록 항목: iteration, actions, verify/reviewer 결과, signature, stall/replan, blocker, next action
