# Build and Test Runbook

## 실행 방법
1. `./gradlew build`
2. `./gradlew lint`
3. `./gradlew test`
4. 또는 `./scripts/verify.sh`

## 실패 시 우선 확인 순서
1. Gradle 실행 권한/Wrapper 동작 확인 (`chmod +x gradlew`)
2. 네트워크/프록시로 wrapper 배포본 다운로드 가능 여부 확인
3. 실패 태스크의 첫 에러 메시지 확인
4. 실패 원인을 current-task loop history에 failure signature로 기록

## 검증 결과 해석
- verify exit code 0: build/lint/test 모두 성공
- verify non-zero: 최소 1개 실패, 완료 선언 금지
- 환경 이슈 실패: blocker로 기록하고 재현 조건 명시
