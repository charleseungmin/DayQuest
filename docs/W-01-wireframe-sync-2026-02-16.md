# W-01 Wireframe Sync (2026-02-16 21:45 KST)

- 기준 프로젝트 경로: `C:\Users\MYCOM\StudioProjects\DayQuest`
- 기준 Figma 파일: `tZyD1oxbfhlLw3a8WoMpKH`
- 작업 유형: 와이어프레임 내부 블록 구조 정리 + 명세 갱신

## 이번 실행에서 반영한 구조 변경

대상 파일: `app/src/main/java/com/dayquest/app/MainActivity.kt`

1. 홈 상단 탭 블록 분리
   - 기존: `DayQuestHome()` 내부에 탭 버튼 Row 하드코딩
   - 변경: `HomeSectionTabs(...)` 컴포저블로 분리
   - 의도: Figma 프레임의 상단 네비게이션 블록과 코드 블록 1:1 대응

2. Task 관리 화면 디버그 상태 전환 블록 카드화
   - 기존: `TaskManageScreen()` 내부 Row 버튼 직접 배치
   - 변경: `TaskStateDebugCard(...)` 컴포저블로 분리 + 카드 래핑
   - 의도: 상태 점검용 UI 블록(Loading/Empty/Error)을 독립 섹션으로 정리

3. 화면 타이틀 텍스트 정리
   - 기존: `TaskManageScreen`
   - 변경: `오늘의 Task`
   - 의도: 와이어프레임 라벨링과 사용자 노출 텍스트 정합성 개선

## 블록 매핑 규칙 (Figma ↔ Compose)

- Frame: Home / Top Navigation → `HomeSectionTabs`
- Frame: Task Manage / State Switcher → `TaskStateDebugCard`
- Frame: Task Manage / Progress Card → `QuestProgressCard`
- Frame: Task Manage / Form Card → `TaskFormCard`
- Frame: Task Manage / List Card → `TaskListCard`

## 다음 10분 작업 후보

- 탭 버튼의 selected 시각 상태를 Figma 스타일(색/보더)로 맞춤
- `TaskListCard` 항목 액션 버튼을 아이콘형으로 축약하여 밀도 개선
- History/Settings 화면도 동일한 블록 분리 패턴으로 정규화
