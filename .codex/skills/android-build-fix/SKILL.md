---
name: android-build-fix
description: Android build/lint/test 실패를 원인별로 분류하고 수정·재검증하는 작업 절차 스킬. verify 실패 분석과 수정이 필요할 때 사용한다.
---

# android-build-fix

## 언제 사용하는지
- `scripts/verify.sh` 또는 개별 Gradle 태스크 실패 시 사용한다.

## 입력 조건
- 실패 로그 원문
- 최근 변경 파일
- 실패 태스크(build/lint/test)

## 실행 절차
1. 실패 태스크를 단독 재실행해 재현한다.
2. failure signature를 1줄로 정의한다.
3. 원인을 코드/설정/환경으로 분류한다.
4. 최소 변경으로 수정하고 관련 docs 갱신 필요 여부를 판정한다.
5. 실패 태스크 재검증 후 `scripts/verify.sh`를 실행한다.
6. 결과를 current-task loop history에 기록한다.

## 실패 시 처리
- 환경 이슈면 blocker로 기록하고 재현 조건을 남긴다.
- 동일 signature 반복이면 planner 재계획 입력으로 전달한다.

## 출력 형식
- 변경 요약
- verify 결과(PASS/FAIL)
- failure signature
- 다음 액션
