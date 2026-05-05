# Architecture Overview

## 저장소 구조 개요
- `AGENTS.md`: 작업 시작 규칙
- `docs/`: 판단 기준 문서
- `.codex/skills/`: 반복 작업 절차
- `scripts/verify.sh`: build/lint/test 검증
- `scripts/check_done.sh`: 종료 가능 판정
- `scripts/detect_stall.sh`: 반복 정체 감지
- `scripts/run_loop.sh`: 외부 루프 오케스트레이션

## 핵심 모듈
- 앱 코드: `app/src/main/java/com/dayquest/...`
- 단위 테스트: `app/src/test/java/com/dayquest/...`
- 빌드 설정: `build.gradle.kts`, `app/build.gradle.kts`, `gradle/wrapper/*`
- 실행 정책: `docs/loop/*`, `docs/review-rules/*`, `docs/exec-plans/current-task.md`

## 영향 범위 판단 기준
- `app/src/main/java` 변경: 기능/회귀 영향 가능, 테스트 재검토 필수
- `app/src/test/java` 변경: 검증 범위 영향, 테스트 의도 문서화 필요
- `docs/review-rules` 변경: reviewer 판정 기준 변경, 체크리스트 재점검 필요
- `scripts/*.sh` 변경: 루프/검증 판정 로직 영향, 실행 로그 확인 필수
- `AGENTS.md` 변경: 시작 규칙 영향, docs와 불일치 여부 확인 필수
