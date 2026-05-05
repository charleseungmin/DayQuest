# G-03 Wireframe Component Contract (Today / Manage)

Last updated: 2026-02-16 21:42 KST  
Owner: UI/UX + Android  
Depends on: `docs/G-02-wireframe-spec-v2.md`

---

## 1) 목적

`G-02`의 화면 레벨 와이어프레임을 **개발 바로 투입 가능한 컴포넌트 계약**으로 고정한다.
이 문서는 다음 3가지를 명시한다.

1. 컴포넌트 API (Compose 파라미터)
2. 상태/이벤트 매핑 규칙
3. QA 검증 포인트 (testTag / acceptance)

---

## 2) Screen State Contract

## 2.1 TodayUiState

```kotlin
data class TodayUiState(
    val dateLabel: String,
    val progress: QuestProgressUi,
    val quickAddInput: String,
    val tasks: List<TodayTaskUi>,
    val isLoading: Boolean,
    val errorMessage: String? = null,
)

data class QuestProgressUi(
    val completionPercent: Int, // 0..100
    val remainingCountToReward: Int,
    val isQuestComplete: Boolean,
)

data class TodayTaskUi(
    val id: Long,
    val title: String,
    val categoryLabel: String?,
    val timeLabel: String?,
    val repeatLabel: String?,
    val isCompleted: Boolean,
    val isOverdue: Boolean,
    val isSkippedToday: Boolean,
    val priority: PriorityUi?,
    val reminderEnabled: Boolean,
)

enum class PriorityUi { LOW, MEDIUM, HIGH }
```

규칙:
- `completionPercent`는 ViewModel에서 0..100으로 clamp 후 전달
- `isLoading == true`면 list 데이터가 있어도 Skeleton 우선 노출
- `errorMessage != null`이면 Error state 우선 노출

## 2.2 ManageUiState

```kotlin
data class ManageUiState(
    val filter: TaskFilterUi,
    val items: List<ManageTaskUi>,
    val isSheetOpen: Boolean,
    val form: TaskFormUi,
    val pendingDeleteTaskId: Long? = null,
)

enum class TaskFilterUi { ALL, ACTIVE, COMPLETED }

data class ManageTaskUi(
    val id: Long,
    val title: String,
    val categoryLabel: String?,
    val repeatSummary: String,
    val goalTimeLabel: String?,
    val isCompleted: Boolean,
)

data class TaskFormUi(
    val mode: FormMode,
    val title: String,
    val category: String?,
    val repeatRule: RepeatRuleUi,
    val customRepeatDays: Set<String>,
    val priority: PriorityUi,
    val goalTimeLabel: String?,
    val reminderEnabled: Boolean,
    val titleError: String? = null,
)

enum class FormMode { CREATE, EDIT }
```

## 2.3 HistoryUiState

```kotlin
data class HistoryUiState(
    val streakDays: Int,
    val recentXp: Int,
    val days: List<HistoryDayUi>,
)

data class HistoryDayUi(
    val dateLabel: String,
    val completedCount: Int,
    val totalCount: Int,
    val earnedXp: Int,
)
```

규칙:
- `streakDays`는 최근 날짜부터 역순으로 연속 완료일 계산
- `recentXp`는 `days`의 earnedXp 합계 (최근 N일 window 기준)

## 2.4 SettingsUiState

```kotlin
data class SettingsUiState(
    val notificationEnabled: Boolean,
    val darkModeEnabled: Boolean,
    val reminderTime: ReminderTimeUi,
)

enum class ReminderTimeUi { H20, H21, H22 }
```

규칙:
- `reminderTime` 변경 시 UI는 Snackbar 이벤트를 1회 발생
- Settings 스위치는 즉시 반영 낙관 업데이트, 실패 시 롤백

---

## 3) Component API (Compose)

## 3.1 TodayRoute

```kotlin
@Composable
fun TodayRoute(
    state: TodayUiState,
    onQuickAddInputChange: (String) -> Unit,
    onQuickAdd: (String) -> Unit,
    onToggleComplete: (Long) -> Unit,
    onSnooze: (Long, Int) -> Unit,
    onSkip: (Long) -> Unit,
    onRetry: () -> Unit,
    onNavigateManage: () -> Unit,
)
```

### testTag
- `today_top_bar`
- `today_progress_card`
- `today_quick_add_input`
- `today_quick_add_button`
- `today_task_list`
- `today_empty_cta`
- `today_error_retry`

## 3.2 ManageRoute

```kotlin
@Composable
fun ManageRoute(
    state: ManageUiState,
    onFilterChange: (TaskFilterUi) -> Unit,
    onClickCreate: () -> Unit,
    onClickEdit: (Long) -> Unit,
    onConfirmDelete: (Long) -> Unit,
    onDismissDelete: () -> Unit,
    onFormValueChange: (TaskFormAction) -> Unit,
    onSubmitForm: () -> Unit,
    onDismissForm: () -> Unit,
)
```

### testTag
- `manage_top_bar`
- `manage_filter_row`
- `manage_task_list`
- `manage_fab`
- `manage_form_sheet`
- `manage_delete_dialog`

## 3.3 HistoryRoute

```kotlin
@Composable
fun HistoryRoute(
    state: HistoryUiState,
)
```

### testTag
- `history_top_bar`
- `history_summary_card`
- `history_day_list`

## 3.4 SettingsRoute

```kotlin
@Composable
fun SettingsRoute(
    state: SettingsUiState,
    onToggleNotification: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onReminderTimeChange: (ReminderTimeUi) -> Unit,
)
```

### testTag
- `settings_top_bar`
- `settings_preference_card`
- `settings_reminder_time_group`

---

## 4) Interaction Rule Matrix

## 4.1 QuickAdd

- 입력값은 `trim()` 기준 1자 이상일 때만 Add 버튼 활성화
- 40자 초과 입력은 UI 단계에서 차단 (`maxLines = 1`, `take(40)`)
- 성공 이벤트 발생 시 순서 고정:
  1) 입력값 clear
  2) 리스트 상단 insert 애니메이션
  3) Snackbar 출력 (`할 일이 추가되었습니다`)

## 4.2 Swipe Action

Today list:
- Start→End: `Snooze(30m)`
- End→Start: `Skip`

Manage list:
- Start→End: `Edit`
- End→Start: `DeleteConfirm`

공통 규칙:
- 스와이프 임계값: 아이템 너비의 35%
- 실행 후 아이템 위치 원복 (dismiss 제거 금지)

## 4.3 Delete Flow

1. 사용자 Delete 스와이프
2. `pendingDeleteTaskId` 세팅 → Dialog 표시
3. `삭제` 탭 시 실제 `onConfirmDelete(taskId)` 호출
4. 성공 시 Snackbar `할 일이 삭제되었습니다`

## 4.4 Settings Reminder Time

1. 사용자가 `20:00/21:00/22:00` 칩 중 하나 선택
2. `onReminderTimeChange(...)` dispatch
3. 상태 반영 후 Snackbar `기본 알림 시간이 HH:mm로 설정되었습니다` 1회 노출

---

## 5) Accessibility / QA Acceptance

## 5.1 접근성 최소 기준

- 터치 타겟 최소 48dp
- 체크박스, FAB, 필터칩에 TalkBack 라벨 부여
- 완료 상태 텍스트 외 시각 요소(아이콘/취소선) 병행

## 5.2 수동 QA 체크리스트

- [x] Today Empty에서 `첫 할 일 만들기` 탭 시 Manage로 이동 (2026-05-05: `today_empty_cta`가 Manage 탭으로 전환)
- [x] QuickAdd 40자 제한 동작 (2026-05-05: ViewModel 입력 단계에서 40자 제한)
- [x] Today swipe 2종(미루기/스킵) 모두 동작 (2026-05-05: 데이터 반영)
- [x] Manage Delete 시 확인 다이얼로그 필수 노출 (wireframe 구현 완료)
- [x] Form 제목 공백 입력 시 저장 불가 + 에러 메시지 노출 (wireframe 구현 완료)
- [x] 회전(세로↔가로) 후 필터/폼 입력 유지 (2026-05-05: 탭/필터/폼 입력을 ViewModel `DayQuestUiState`에 보관)
- [x] 탭 전환 후 탭별 스크롤 위치 유지 (2026-05-05: 각 탭 `LazyListState`를 root에서 보관)

---

## 6) Figma Frame / Export Naming Rule

프레임 이름 규칙:
`DQ/{Screen}/{State}/{Variant}`

예시:
- `DQ/Today/Content/Default`
- `DQ/Today/Empty/NoTask`
- `DQ/Manage/Sheet/Create`
- `DQ/Manage/Dialog/DeleteConfirm`

캡처 파일명 규칙:
`wireframe_{screen}_{state}_{yyyymmdd}.png`

예시:
- `wireframe_today_content_20260216.png`
- `wireframe_manage_sheet_create_20260216.png`

---

## 7) 구현 착수 우선순위

1. `TodayRoute` 상태 분기 + testTag 부착
2. `QuickAddRow` 제약(40자/활성화) 확정
3. `ManageRoute` + Form/삭제 다이얼로그 상태 관리 ✅ (2026-02-16 wireframe 반영)
4. Swipe action의 공통 임계값/원복 동작 유틸화

## 8) 2026-05-05 Full-Planning Contract Update

- `TaskFormUi` 구현은 `TaskFormSheet`에서 전체 기획 필드를 노출한다.
- `categoryLabel`, `reminderEnabled`, `isSkippedToday`는 Room에 저장된다.
- `repeatRule = 커스텀`은 월/화/수/목/금/토/일 요일 칩을 표시하고 `커스텀:월,수,금` 형태로 저장한다.
- 커스텀 반복 알림은 선택된 요일만 대상으로 계산한다.
- `snooze`는 기존 목표 시간이 있으면 그 시간 기준, 없으면 현재 시간 기준 +30분으로 `timeLabel`을 갱신한다.
- `skip`은 오늘 날짜만 숨김 처리하며 Manage 목록에는 남긴다.
