package com.dayquest.app.ui.logic

import com.dayquest.app.ui.model.QuestProgressUi
import com.dayquest.app.ui.model.TaskItemUi

object QuestFeedbackLogic {
    fun progress(tasks: List<TaskItemUi>): QuestProgressUi {
        val done = tasks.count { it.isDone }
        return QuestProgressUi(doneCount = done, totalCount = tasks.size)
    }

    fun shouldCelebrate(before: List<TaskItemUi>, after: List<TaskItemUi>): Boolean {
        val prev = progress(before)
        val next = progress(after)
        return prev.totalCount > 0 && prev.doneCount < prev.totalCount && next.totalCount > 0 && next.doneCount == next.totalCount
    }
}
