---
name: pr-review-checklist
description: PR 체크리스트와 done-criteria를 기준으로 reviewer pass/fail을 판정하는 절차 스킬. 검토 누락과 회귀 위험을 확인할 때 사용한다.
---

# pr-review-checklist

## 언제 사용하는지
- implementer 변경 이후 reviewer 단계에서 사용한다.

## 입력 조건
- 변경 diff
- verify 결과
- `docs/review-rules/pr-checklist.md`
- `docs/review-rules/done-criteria.md`

## 실행 절차
1. 요구사항 충족 여부를 Goal/Scope 대비로 점검한다.
2. 회귀 가능성과 테스트 존재 여부를 점검한다.
3. 문서/loop 기록 최신화를 점검한다.
4. UI/문구 영향 여부를 점검한다.
5. 항목별 pass/fail 근거를 작성한다.
6. reviewer 최종 결과와 failure signature를 기록한다.

## 실패 시 처리
- FAIL 항목을 수정 요구 목록으로 구조화한다.
- 동일 FAIL signature 반복 시 planner 재계획 신호로 전달한다.

## 출력 형식
- reviewer result(PASS/FAIL)
- failure signature
- 수정 요구 항목
