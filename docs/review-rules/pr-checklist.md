# PR Checklist

## Pass/Fail 체크리스트
- [ ] 요구사항 충족: Goal/Scope 대비 변경이 일치한다.
- [ ] 회귀 가능성 점검: 영향 영역과 위험 완화 조치가 기록됐다.
- [ ] 테스트 존재 여부: 신규/변경 로직에 맞는 테스트 또는 사유가 있다.
- [ ] 문서 갱신 여부: 판단 기준/실행 계획/루프 기록이 최신이다.
- [ ] UI/문구 영향 여부: 변경 시 스크린/문구 영향 검토가 기록됐다.
- [ ] verify 결과: `scripts/verify.sh` 최신 결과가 PASS다.

## Reviewer 출력 규칙
- 결과는 `PASS` 또는 `FAIL`만 사용한다.
- FAIL이면 수정 요구 항목과 failure signature를 함께 남긴다.
