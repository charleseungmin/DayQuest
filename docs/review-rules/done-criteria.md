# Done Criteria

## 판정표 (Pass/Fail)
| 항목 | Pass 기준 | Fail 기준 |
|---|---|---|
| build 성공 | `verify.sh`에서 build 성공 로그 확인 | build 실패 또는 미실행 |
| lint 성공 | `verify.sh`에서 lint 성공 로그 확인 | lint 실패 또는 미실행 |
| test 성공 | `verify.sh`에서 test 성공 로그 확인 | test 실패 또는 미실행 |
| 요구사항 충족 | Goal/Scope 대비 구현·문서가 일치 | 누락/범위 이탈 존재 |
| reviewer pass | reviewer 결과가 `PASS` | `FAIL` 또는 미판정 |
| 문서 갱신 완료 | 관련 docs와 loop history 최신화 | docs 누락 또는 구버전 |
| current-task 최신화 완료 | Latest loop entry와 Next step 업데이트 | 최신 반복 정보 누락 |

## 종료 규칙
- 모든 항목이 Pass일 때만 종료 가능
- 하나라도 Fail이면 loop를 계속 수행
