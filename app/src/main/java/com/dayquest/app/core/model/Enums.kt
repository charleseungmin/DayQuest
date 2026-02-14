package com.dayquest.app.core.model

enum class TaskType {
    ROUTINE,
    GOAL,
    ONE_TIME
}

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH
}

enum class RepeatType {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

enum class DailyItemStatus {
    TODO,
    DONE,
    DEFERRED,
    SKIPPED
}

enum class QuestType {
    COMPLETE_ONE,
    COMPLETE_ALL,
    COMPLETE_IMPORTANT
}
