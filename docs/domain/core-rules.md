# Core Domain Rules

## 도메인 핵심 규칙
- 요구사항은 검증 가능한 단위로 분해한다.
- 범위 밖 변경은 planner 재승인 없이 수행하지 않는다.
- pass/fail 근거는 문서 기준으로만 판정한다.

## 수정 금지/주의 영역
- `gradle/wrapper/*`: 버전 변경 시 빌드 재현성 영향이 커서 근거 없이 수정 금지
- `docs/review-rules/*`: 판정 기준이므로 임의 완화 금지
- `scripts/check_done.sh`: 성공 종료 기준 우회 로직 추가 금지
- `scripts/run_loop.sh`: 반복 제한(최대 횟수/강제 종료) 추가 금지

## 구현 판단 기준
- verify 실패 상태에서 성공 보고 금지
- reviewer FAIL 상태에서 완료 처리 금지
- stall 감지는 종료가 아니라 planner 재계획 신호로 처리
- done-criteria 미충족 시 반복을 유지하고 기록을 남긴다
