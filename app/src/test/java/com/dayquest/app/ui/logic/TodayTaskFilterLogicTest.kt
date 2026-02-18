package com.dayquest.app.ui.logic

import com.dayquest.app.ui.model.TaskItemUi
import org.junit.Assert.assertEquals
import org.junit.Test

class TodayTaskFilterLogicTest {

    private val tasks = listOf(
        TaskItemUi(id = "1", title = "진행중", category = "일반", isDone = false, isDeferred = false),
        TaskItemUi(id = "2", title = "완료", category = "일반", isDone = true, isDeferred = false),
        TaskItemUi(id = "3", title = "미룸", category = "일반", isDone = false, isDeferred = true)
    )

    private val sortTasks = listOf(
        TaskItemUi(id = "a", title = "라면", category = "생활", isImportant = false, isDone = false, isDeferred = false),
        TaskItemUi(id = "b", title = "가계부", category = "생활", isImportant = true, isDone = false, isDeferred = false),
        TaskItemUi(id = "c", title = "나들이", category = "취미", isImportant = false, isDone = true, isDeferred = false),
        TaskItemUi(id = "d", title = "다이어리", category = "취미", isImportant = true, isDone = false, isDeferred = true)
    )

    @Test
    fun `filterTodayTasks returns tasks by status`() {
        assertEquals(listOf("1"), filterTodayTasks(tasks, TodayTaskFilter.InProgress).map { it.id })
        assertEquals(listOf("2"), filterTodayTasks(tasks, TodayTaskFilter.Done).map { it.id })
        assertEquals(listOf("3"), filterTodayTasks(tasks, TodayTaskFilter.Deferred).map { it.id })
        assertEquals(listOf("1", "2", "3"), filterTodayTasks(tasks, TodayTaskFilter.All).map { it.id })
    }

    @Test
    fun `buildTodayFilterCounts aggregates each bucket`() {
        val counts = buildTodayFilterCounts(tasks)

        assertEquals(3, counts[TodayTaskFilter.All])
        assertEquals(1, counts[TodayTaskFilter.InProgress])
        assertEquals(1, counts[TodayTaskFilter.Done])
        assertEquals(1, counts[TodayTaskFilter.Deferred])
    }

    @Test
    fun `sortTodayTasks recommended puts important then active first`() {
        val sorted = sortTodayTasks(sortTasks, TodayTaskSort.Recommended)

        assertEquals(listOf("b", "d", "a", "c"), sorted.map { it.id })
    }

    @Test
    fun `sortTodayTasks title ascending orders by Korean title`() {
        val sorted = sortTodayTasks(sortTasks, TodayTaskSort.TitleAscending)

        assertEquals(listOf("b", "c", "d", "a"), sorted.map { it.id })
    }
}
