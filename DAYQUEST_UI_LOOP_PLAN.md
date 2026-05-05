# DAYQUEST UI LOOP PLAN

## Loop #1 (기존)
- Today/Manage/History/Settings 와이어프레임 초안 구성
- testTag, swipe 액션, 접근성 contentDescription, CTA 이동, 삭제 snackbar 반영

## Loop #2 (Stitch 기준 정렬)
Status: Superseded by `docs/W-14-stitch-guild-board-theme-2026-04-23.md`, `docs/G-02-wireframe-spec-v2.md`, and `docs/G-03-wireframe-component-contract.md`.

### 목표
- Today 화면을 Stitch 공유 시안에 가깝게 재구성
- 5-state 토글 체계(content/loading/error/empty/success) 명확화
- Loop #1 품질요소(testTag/swipe/접근성/CTA/snackbar) 유지

### 진행
- Today 상단 헤더를 `DayQuest + 날짜 + 연속달성` 구조로 분리(`TodayHeaderUi`, `TodayHeaderSection`)
- Hero 카드에 `오늘의 퀘스트 진행도`, 진행률 %, 레벨/XP 텍스트를 강조
- 메인 퀘스트 카드(`MainQuestCard`) 추가
- quick add placeholder를 `새로운 태스크 추가...`로 교체
- 연결된 태스크 카드에 명시 액션 버튼 `건너뛰기/미루기/완료` 추가
- 하단 내비 라벨을 `오늘/관리/기록/설정`으로 일관화
- `TodayViewState`를 5개 상태로 확장하고 칩 토글/표시 라벨 정비
- 상태 자동 보정 로직 추가: 콘텐츠에서 태스크 없음→EMPTY, 전체 완료→SUCCESS

### 상태 정리
- Stitch 공유 원본의 픽셀 단위 스펙(간격/폰트/컬러 토큰)은 현재 접근 가능한 자료에 없음
  - 2026-05-05: 로컬 기준 색상/타이포/Shape 토큰은 `DayQuestTokens.kt`로 분리 완료. Drive/Stitch 원본 픽셀 스펙 접근 없이는 1:1 검증 불가.
- 날짜/연속달성/레벨/XP 설계는 W-14에서 재정의됨
  - 2026-05-05: W-14 결정으로 Today의 레벨/플레이어 XP 카드는 제거. 연속 달성은 History 실데이터 기반으로 Today/History에 반영, XP는 History 요약에 표시.
- 루프 기록은 현재 문서화되어 있음
  - 2026-05-05: `docs/exec-plans/current-task.md`에 iteration 1-10 기록 유지.

### 다음 할 일 Top3
1. Drive 원본 기획자료 접근 후 로컬 문서에 없는 기능이 있는지 재감사
2. 접근 가능한 Stitch/Drive 원본 픽셀 스펙이 제공되면 색상/타이포/여백의 1:1 정합도 재검증
3. 원본 기획자료에서 요구하는 추가 QA 캡처 목록이 확인되면 문서화
