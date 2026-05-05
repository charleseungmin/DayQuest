# G-02 Wireframe Spec v2 (Today / Manage)

Last updated: 2026-02-16
Owner: UI/UX + Android
Scope: Low-fi wireframe를 개발 반영 가능한 Compose UI 명세로 고정

---

## 1) 공통 레이아웃 규칙

- 기준 해상도: 360x800dp (Android small baseline)
- Safe area 제외 후 콘텐츠 좌우 패딩: `16dp`
- Vertical rhythm: `8dp` 그리드 (8/12/16/24/32)
- Corner radius
  - Card: `12dp`
  - Input/FAB small chip: `10dp`
  - Bottom sheet top radius: `20dp`
- Typography (임시)
  - H1(Screen title): 24sp / SemiBold
  - H2(Section): 18sp / SemiBold
  - Body: 14sp / Regular
  - Caption: 12sp / Regular

---

## 2) Navigation wireframe

## Bottom tab (고정 4개)
1. Today
2. Manage
3. History
4. Settings

규칙:
- 현재 탭만 label 노출, 나머지는 아이콘만 노출 가능
- 탭 전환 시 스크롤 위치는 탭별 보존

---

## 3) Today Screen 상세 와이어프레임

## 3.1 화면 구조 (top -> bottom)
1. TopAppBar
   - 좌: `Today`
   - 우: 날짜 텍스트 (`Mon, Feb 16`) + 필터 아이콘
2. QuestProgressCard
3. QuickAddRow
4. SectionHeader (`오늘 일정` + 정렬 드롭다운)
5. TodayTaskList (LazyColumn)

## 3.2 컴포넌트 명세

### A. QuestProgressCard
- 높이: min `112dp`
- 내부:
  - Title: `오늘의 퀘스트`
  - Progress bar: 0~100%
  - Subtext: `다음 보상까지 n개`
- 상태:
  - 0%: 보상 문구 대신 `첫 완료를 시작하세요`
  - 100%: 배너 스타일 강조 (`Quest Complete`)

### B. QuickAddRow
- 구성: TextField + Add 버튼
- 높이: `48dp`
- Placeholder: `할 일을 빠르게 추가`
- 입력 제한: 제목 40자 (초과 입력 차단)
- Add 활성화 조건: trim 후 1자 이상
- 성공 시:
  - 입력창 clear
  - 리스트 최상단 삽입
  - Snackbar `할 일이 추가되었습니다`

### C. TodayTaskItem
- 최소 높이: `72dp`
- 좌측: 중요도 배지(선택) + 체크박스
- 중앙: 제목(1줄 ellipsis) / 메타(시간, 반복)
- 우측: overflow 메뉴 또는 swipe action 힌트
- 인터랙션:
  - 체크박스 탭 -> 완료 처리
  - 우스와이프 -> 미루기
  - 좌스와이프 -> 스킵
- 상태:
  - overdue: 시간 텍스트 warning color
  - completed: 제목 strike-through + 60% alpha

### D. Empty / Loading / Error
- Empty:
  - 아이콘 + `오늘 할 일이 없습니다`
  - CTA: `첫 할 일 만들기` (Manage 이동)
- Loading:
  - item skeleton 3개
- Error:
  - 메시지 + Retry 버튼

## 3.3 Today 이벤트 계약 (UI -> ViewModel)
- `onQuickAdd(title: String)`
- `onToggleComplete(taskId: Long)`
- `onSnooze(taskId: Long, minutes: Int = 30)`
- `onSkip(taskId: Long)`
- `onRetryLoad()`

---

## 4) Manage Screen 상세 와이어프레임

## 4.1 화면 구조
1. TopAppBar (`Manage`)
2. Filter chips row (All / Active / Completed)
3. TaskList
4. FAB (`+`)
5. FAB 탭 시 TaskForm BottomSheet

## 4.2 컴포넌트 명세

### A. TaskListItem
- 높이: `76dp`
- 제목 + 카테고리 칩 + 반복 규칙 요약
- trailing: 목표시간(선택), 더보기 메뉴
- swipe:
  - start->end: Edit
  - end->start: Delete(확인 다이얼로그)

### B. TaskFormSheet (Create/Edit 공용)
필드 순서:
1. 제목(필수)
2. 카테고리(선택)
3. 반복 규칙(매일/주중/주말/커스텀)
4. 중요도(낮음/중간/높음)
5. 목표시간(선택)
6. 알림 토글

버튼:
- Primary: 저장
- Secondary: 취소

검증 규칙:
- 제목 공백 불가
- 제목 40자 제한
- 목표시간은 과거 시간 허용(다음 반복 기준으로 해석)

### C. Delete 확인 다이얼로그
- 제목: `할 일을 삭제할까요?`
- 본문: `삭제 후 복구할 수 없습니다.`
- 액션: 취소 / 삭제

## 4.3 Manage 이벤트 계약
- `onCreateTask(draft: TaskDraft)`
- `onUpdateTask(taskId: Long, draft: TaskDraft)`
- `onDeleteTask(taskId: Long)`
- `onFilterChange(filter: TaskFilter)`

---

## 5) History / Settings Screen 와이어프레임 (v2 추가)

## 5.1 History 화면 구조
1. TopAppBar (`History`)
2. SummaryCard (연속 달성일 / 최근 누적 XP)
3. DailyHistoryList (최근 7일 기준)

### History 컴포넌트 규칙
- SummaryCard
  - 좌: `연속 달성 n일`
  - 우: `최근 누적 XP`
- DailyHistoryItem
  - dateLabel (`2/16 (Mon)`)
  - completion ratio (`완료 2/3`)
  - earned XP (`+45 XP`)

## 5.2 Settings 화면 구조
1. TopAppBar (`Settings`)
2. PreferenceCard (`알림`, `다크 모드` 스위치)
3. ReminderTimeCard (20:00/21:00/22:00 단일 선택 Chip)

### Settings 컴포넌트 규칙
- 알림 스위치: daily reminder on/off
- 다크 모드 스위치: 앱 강제 테마 옵션
- 기본 알림 시간 선택 시 Snackbar 출력
  - `기본 알림 시간이 HH:mm로 설정되었습니다`

---

## 6) Compose 구현 체크리스트 (개발 반영용)

- [x] `TodayRoute` 상태 분기: Loading/Empty/Error/Content *(wireframe prototype: `DayQuestWireframeApp`, state toggle chips 기반)*
- [x] `QuestProgressCard` 컴포넌트 분리 *(wireframe prototype: `QuestProgressCard`)*
- [x] `QuickAddRow` 입력 제한 + snackbar 연결 *(wireframe prototype: `DayQuestWireframeApp`)*
- [x] `TodayTaskItem` swipe action 2종(미루기/스킵) *(2026-05-05: snooze/skip 데이터 반영)*
- [x] `ManageRoute` + Filter chips 상태 *(wireframe prototype: `DayQuestWireframeApp`)*
- [x] `TaskFormSheet` (create/edit 공용) *(2026-05-05: 카테고리/반복/중요도/목표시간/개별알림 포함)*
- [x] `TaskListItem` delete confirm dialog
- [x] `HistoryRoute` 요약 카드 + 일별 완료/XP 리스트 와이어프레임
- [x] `SettingsRoute` 알림/다크모드 스위치 + 기본 알림 시간 칩
- [x] UI 이벤트를 ViewModel intent로만 전달 (direct repo 호출 금지)
- [x] 컴포넌트 API/상태 계약 문서 분리 (`docs/G-03-wireframe-component-contract.md`)

### 6.1 프로토타입 반영 내역 (2026-02-16)

- 신규 파일: `app/src/main/java/com/dayquest/app/ui/DayQuestWireframe.kt`
  - Bottom tab 4개(Today/Manage/History/Settings) 와이어프레임 렌더
  - Today: QuestProgressCard, QuickAddRow(40자 제한/trim 기반 활성화), Task list 완료 토글, Empty state
  - Manage: Filter chips(All/Active/Completed), Task list 기본 레이아웃, FAB 노출
  - History: 연속 달성/누적 XP SummaryCard + 일별 완료/XP 카드 리스트
  - Settings: 알림/다크모드 스위치, 기본 알림 시간(20/21/22시) 단일 선택 칩 + 변경 Snackbar
- `MainActivity`를 임시 텍스트 화면에서 `DayQuestWireframeApp()` 렌더로 교체

### 6.2 추가 반영 내역 (2026-02-16, cron: wireframe-10min-progress)

- `TodayScreenWireframe`에 상태 분기 `TodayViewState(Content/Loading/Error)` 추가
  - Loading: 72dp skeleton card 3개
  - Error: 실패 메시지 + Retry 버튼으로 Content 복귀
  - Content: 기존 Empty/TaskList 유지
- `QuestProgressCard`를 별도 컴포저블로 분리하고 진행률 계산을 동적화
  - 0%: `첫 완료를 시작하세요`
  - 100%: `Quest Complete`
  - 그 외: `다음 보상까지 n개`
- Today 탭에 상태 토글 칩(Content/Loading/Error) 추가하여 상태별 와이어프레임 검증 가능

---

## 7) 오픈 이슈 (다음 작업)

1. Drive 원본 기획자료 접근 후 로컬 문서에 없는 추가 기능이 있는지 재감사 필요

### 7.1 추가 반영 내역 (2026-05-05, full-planning expansion)

- Stitch MCP 신규 시안: `projects/10478953360846233948/screens/f7c8cc48f07b4292ac7f0ebbd496a2e3`
- `TaskFormSheet` 전체 기획 필드 반영:
  - 제목
  - 카테고리
  - 반복 규칙(매일/주중/주말/커스텀)
  - 커스텀 반복 요일 선택(월/화/수/목/금/토/일)
  - 의뢰 등급(낮음/중간/높음)
  - 목표 시간(HH:mm)
  - 개별 알림 토글
- Today swipe action 실제 반영:
  - 미루기: 목표 시간을 30분 뒤로 갱신
  - 스킵: 오늘 날짜 기준으로 Today에서 숨김
- History:
  - 최근 30일 window를 생성해 0건 날짜도 표시
  - 연속 달성, 최근 누적 XP, 달성률 요약 표시

### 7.2 추가 반영 내역 (2026-05-05, custom-repeat expansion)

- Stitch MCP 신규 시안: `projects/10478953360846233948/screens/049a3fbf906a491ea600c558d09b478a`
- `커스텀` 반복 선택 시 월/화/수/목/금/토/일 요일 칩 표시
- 기본 선택은 월/수/금이며, 사용자가 각 요일을 토글 가능
- 저장 형식은 `커스텀:월,수,금` 형태
- 리마인더 스케줄러가 선택된 커스텀 요일을 실제 알림 요일로 해석

### 7.3 추가 반영 내역 (2026-05-05, QA/accessibility closeout)

- 색상/타이포/Shape 토큰을 `DayQuestTokens.kt`로 분리하고 `DayQuestTheme.kt`는 MaterialTheme 조립만 담당하도록 정리
- Settings 상태 분기를 ViewModel 레벨로 승격:
  - `settingsLoadState: LoadState`
  - Loading skeleton
  - Error retry CTA
  - Content preference cards
- 접근성 기준 반영:
  - FAB, 필터칩, 주요 스위치, CTA, QuickAdd 버튼에 TalkBack 라벨 추가
  - 주요 칩 터치 타겟을 48dp 이상으로 정렬
- 수동 QA 체크리스트 반영:
  - Today Empty CTA는 Manage 탭으로 이동
  - QuickAdd는 입력 단계에서 40자 제한
  - 탭/필터/폼 입력은 ViewModel 상태에 있어 회전 후 유지
  - 각 탭의 `LazyListState`를 root에서 보관해 탭 전환 후 스크롤 위치 유지

### 7.4 추가 반영 내역 (2026-05-05, Drive source recheck)

- Google Drive 커넥터가 `최승민 <chltmdals654@gmail.com>` 계정으로 연결되어 원본 폴더 `DayQuest (Task 관리 앱)` 문서 4개를 확인했다.
- 원본 설계/요구사항 문서에서 확인한 Task 필드 일부를 추가 반영:
  - 메모
  - 반복 규칙 `매달:n`
  - 시작일/종료일 `yyyy-MM-dd`
- Today 목록과 알림 계획이 시작일/종료일, 주중/주말, 커스텀 요일, 매달 반복일을 기준으로 현재 날짜 대상 의뢰만 사용하도록 정리했다.
- 리마인더 스케줄러에 원본 요구사항의 07:00 고정 알림 후보를 추가했다.
- 아직 남은 원본 요구사항 격차는 `docs/exec-plans/full-scope-completion-audit-2026-05-05.md`에 추적한다.

### 7.5 추가 반영 내역 (2026-05-05, DailyItem/Quest expansion)

- 원본 요구사항의 `DailyItem(date/status/source/doneAt)` 모델을 Room 테이블로 추가했다.
  - 기본 키는 `(taskId, date)`로 두어 같은 날짜 중복 생성을 방지한다.
  - 상태는 `TODO/DONE/DEFERRED/SKIPPED`를 사용한다.
- Today 완료/스킵/미루기 상태를 DailyItem 기준으로 저장한다.
  - 완료는 오늘 DailyItem을 `DONE`으로 저장하고 기존 완료 로그도 유지한다.
  - 스킵은 오늘 DailyItem을 `SKIPPED`로 저장해 Quest 집계와 Today 표시에서 제외한다.
  - 미루기는 오늘 DailyItem을 `DEFERRED`로 저장하고 내일 `source=DEFERRED` 항목을 생성한다.
- Task 수정 시 오늘 반복 조건에서 빠진 항목은 미완료 상태만 제거하고, 이미 완료한 오늘 항목은 보호한다.
- Today에 `META_ONE`, `META_ALL`, `IMPORTANT_ONE` Quest 보드를 추가했다.
- History는 날짜별 활성 의뢰 수 기준으로 달성률/스트릭을 계산하고, 완료 로그의 상위 분야 누적을 표시한다.
- 알림 정책은 원본 요구사항에 맞춰 고정 07:00/21:00 + 목표시간 알림으로 정리했다.

### 7.6 추가 반영 내역 (2026-05-05, source closeout)

- Quick Add 상세 옵션을 추가했다.
  - 메모
  - 목표 시간 `HH:mm`
  - 반복 `단발성/매일/주중/주말`
- Task 저장 모델에 `isActive`, `createdAtEpochMillis`를 추가했다.
- Manage 목록 정렬은 미완료 우선, 동일 상태 내 등급 우선으로 정리했다.
- Quest 달성 시 Today에서 스낵바로 인앱 알림을 노출한다.
- Daily Quest도 Room 테이블에 저장해 `META_ONE`, `META_ALL`, `IMPORTANT_ONE`의 상태/진행도/달성 시각을 보존한다.
- 데이터 초기화는 원본 요구사항대로 Task/DailyItem/Quest/History를 비우고 샘플 Task를 재생성하지 않는다.
- History 달성률/스트릭은 DailyItem의 `TODO/DONE`만 집계해 `SKIPPED/DEFERRED`가 streak에 영향을 주지 않도록 했다.

### 7.7 추가 반영 내역 (2026-05-05, character growth reward loop)

- 일일 todo 완료 보상을 캐릭터 성장으로 연결했다.
  - 완료 시 Task 등급에 따라 성장 XP 지급: 높음 60, 중간 40, 낮음 25
  - Task 카테고리에 따라 성장 스탯 지급: 업무=집중, 건강=체력, 학습=통찰, 그 외=균형
- 캐릭터 상태는 Today 상단의 `캐릭터 성장` 카드로 표시한다.
  - 레벨/칭호
  - 현재 레벨 EXP 진행도
  - 누적 EXP
  - 훈련 포인트
  - 집중/체력/통찰/균형 스탯
- 보상은 `character_reward_logs`에 `(taskId, date)` 기준으로 저장한다.
  - 같은 일일 todo 완료는 중복 보상되지 않는다.
  - 완료 취소 시 보상 로그를 제거하고 캐릭터 상태를 전체 보상 로그 기준으로 재계산한다.
- 캐릭터 상태는 `character_progress`에 저장하며 앱 재시작 후에도 유지한다.
- 데이터 초기화 시 캐릭터 보상 로그와 성장 상태도 초기화한다.

### 7.8 추가 반영 내역 (2026-05-05, character graphics)

- Today 상단의 `캐릭터 성장` 카드에 Compose Canvas 기반 캐릭터 그래픽을 추가했다.
- 그래픽은 외부 PNG/Krita 파일 없이 앱 코드에서 직접 렌더링한다.
  - Lv.1~3: 기본 모험가 실루엣
  - Lv.4~6: 성장 오라와 망토
  - Lv.7~9: 지팡이/장비 연출
  - Lv.10+: 왕관과 강화 오라
- 집중/체력/통찰/균형 스탯은 캐릭터 하단의 작은 색상 오브로 함께 시각화한다.
- 접근성 설명은 `레벨 N 칭호 캐릭터 그래픽` 형식으로 제공한다.
- 추후 Krita로 제작한 PNG/애니메이션 에셋을 사용할 경우에도 현재 `CharacterAvatar` 진입점을 이미지 렌더러로 교체하면 된다.
