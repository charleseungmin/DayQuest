# W-14 Stitch Guild Board Theme

Date: 2026-04-23
Owner: Codex

## Goal
- Stitch MCP로 생성한 `DayQuest` 길드 게시판 톤을 현재 Compose wireframe에 반영한다.
- 실제 기능 범위만 남기고, `Today / Manage / History / Settings` 4탭 구조를 유지한다.

## Stitch Assets
- Project: `projects/10478953360846233948`
- Light Today Screen: `projects/10478953360846233948/screens/e947ef615592417d80d413a28910b5dd`
- Dark Today Screen: `projects/10478953360846233948/screens/2b6606c80d75418982be117ea9a84123`
- Manage Screen: `projects/10478953360846233948/screens/0453ee4452714b0b9b668554793eeab5`
- History Screen: `projects/10478953360846233948/screens/b3128e48c3644f6787991f8e5b22664c`
- Settings Screen: `projects/10478953360846233948/screens/377e7d2c6cbb4ee089e9d36aded94d00`
- Full Task Planning Sheet: `projects/10478953360846233948/screens/f7c8cc48f07b4292ac7f0ebbd496a2e3`
- Custom Repeat Planning Sheet: `projects/10478953360846233948/screens/049a3fbf906a491ea600c558d09b478a`
- Design System Asset: `assets/da629ddf9e044f6198d40039a8df87b3`

## Applied Design Tokens
- Light background: warm parchment `#FFF9ED`
- Light surfaces: layered vellum `#FCF3D8`, `#EBE2C8`
- Dark background: candlelit ledger `#17140F`
- Dark surfaces: dark brown ledger `#211C17`, `#2D2721`
- Primary accent: muted bronze `#705836` / dark mode `#B8936B`
- Secondary accent: olive family `#5C614D`
- Active accent: muted teal `#356363`
- Typography: serif headlines + clean sans body

## UI Decisions
- `Today`
  - 날짜 헤더, 진행도 카드, 스트릭 카드, 빠른 추가 행, 의뢰 리스트로 단순화
  - XP, 레벨, 플레이어 카드 제거
- `Manage`
  - 필터 + 의뢰 카드 + 수정/삭제 플로우 유지
  - MVP 범위를 넘어 기획서의 전체 TaskForm 필드(제목, 카테고리, 반복 규칙, 의뢰 등급, 목표 시간, 개별 알림)를 bottom sheet에 반영
  - `Full Task Planning Sheet` Stitch 시안을 기준으로 카테고리/반복/등급은 chip 기반으로 정리
  - Stitch 시안처럼 상단을 카드 대신 간결한 헤더로 정리
- `History`
  - 기획서 기준 연속 달성, 최근 누적 XP, 달성률을 모두 표시
  - 최근 30일 기록을 0건 날짜까지 포함해 연속 달성 계산 누락을 줄임
- `Settings`
  - 알림, 다크 모드, 알림 시간, 초기화, 버전 정보만 노출

## Implementation Mapping
- Theme tokens: `app/src/main/java/com/dayquest/app/ui/theme/DayQuestTokens.kt`
- Theme assembly: `app/src/main/java/com/dayquest/app/ui/theme/DayQuestTheme.kt`
- Main wireframe layout: `app/src/main/java/com/dayquest/app/ui/DayQuestWireframe.kt`
- User-facing state/messages: `app/src/main/java/com/dayquest/app/ui/DayQuestViewModel.kt`
- Task draft validation and snooze policy: `app/src/main/java/com/dayquest/domain/TaskRules.kt`
- Persistent full task fields and skip/snooze actions: `app/src/main/java/com/dayquest/data/RoomDayQuestRepository.kt`
- Room schema migration v1 -> v2: `app/src/main/java/com/dayquest/data/DayQuestDatabase.kt`

## Validation
- `./gradlew :app:compileDebugKotlin --console=plain`
- `scripts/verify.sh` via `C:\\Program Files\\Git\\bin\\bash.exe`

## 2026-05-05 Full-Planning Expansion
- Added Stitch reference for the full planning bottom sheet.
- Added persistent fields:
  - `categoryLabel`
  - `reminderEnabled`
  - `skippedDate`
- Added actual data mutations for Today swipe actions:
  - Snooze: recalculates `timeLabel` by +30 minutes and refreshes reminders.
  - Skip: marks the task skipped for the current local date and hides it from Today while keeping it in Manage.
- Reintroduced History XP summary because the current goal asks for every planning feature, not only the previous narrowed MVP/design scope.
- Added custom repeat weekday selection:
  - Stitch reference: `projects/10478953360846233948/screens/049a3fbf906a491ea600c558d09b478a`
  - UI stores repeat rules as `커스텀:월,수,금` style values.
  - Reminder scheduling maps selected Korean weekday chips to actual `DayOfWeek` values.
