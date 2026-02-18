package com.dayquest.app.ui.logic

import com.dayquest.app.core.model.TaskPriority
import com.dayquest.app.ui.model.TaskItemUi

enum class TodayTaskFilter(val label: String) {
    All("전체"),
    InProgress("진행중"),
    Done("완료"),
    Deferred("미룸")
}

fun filterTodayTasks(tasks: List<TaskItemUi>, filter: TodayTaskFilter): List<TaskItemUi> {
    return when (filter) {
        TodayTaskFilter.All -> tasks
        TodayTaskFilter.InProgress -> tasks.filter { !it.isDone && !it.isDeferred }
        TodayTaskFilter.Done -> tasks.filter { it.isDone }
        TodayTaskFilter.Deferred -> tasks.filter { it.isDeferred }
    }
}

fun buildTodayFilterCounts(tasks: List<TaskItemUi>): Map<TodayTaskFilter, Int> = mapOf(
    TodayTaskFilter.All to tasks.size,
    TodayTaskFilter.InProgress to tasks.count { !it.isDone && !it.isDeferred },
    TodayTaskFilter.Done to tasks.count { it.isDone },
    TodayTaskFilter.Deferred to tasks.count { it.isDeferred }
)

enum class TodayTaskSort(val label: String) {
    Recommended("추천순"),
    PriorityFirst("우선순위"),
    ImportantFirst("중요 우선"),
    TitleAscending("제목 가나다")
}

fun sortTodayTasks(tasks: List<TaskItemUi>, sort: TodayTaskSort): List<TaskItemUi> {
    val priorityRank: (TaskItemUi) -> Int = {
        when (it.priority) {
            TaskPriority.HIGH -> 0
            TaskPriority.MEDIUM -> 1
            TaskPriority.LOW -> 2
        }
    }

    val recommendedComparator = compareBy<TaskItemUi> { priorityRank(it) }
        .thenByDescending { it.isImportant }
        .thenBy { if (it.isDone) 1 else 0 }
        .thenBy { if (it.isDeferred) 1 else 0 }
        .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }

    return when (sort) {
        TodayTaskSort.Recommended -> tasks.sortedWith(recommendedComparator)
        TodayTaskSort.PriorityFirst -> tasks.sortedWith(
            compareBy<TaskItemUi> { priorityRank(it) }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        )

        TodayTaskSort.ImportantFirst -> tasks.sortedWith(
            compareByDescending<TaskItemUi> { it.isImportant }
                .thenBy { priorityRank(it) }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        )

        TodayTaskSort.TitleAscending -> tasks.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    }
}
