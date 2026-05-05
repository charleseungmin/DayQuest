package com.dayquest.domain

data class CharacterLevelProgress(
    val level: Int,
    val expInLevel: Int,
    val nextLevelExp: Int,
    val totalExp: Int,
)

object CharacterGrowthRules {
    const val STAT_FOCUS = "FOCUS"
    const val STAT_VITALITY = "VITALITY"
    const val STAT_INSIGHT = "INSIGHT"
    const val STAT_BALANCE = "BALANCE"

    fun rewardXpForTier(tier: String): Int = when (tier) {
        "MAIN" -> 60
        "RARE" -> 40
        else -> 25
    }

    fun statForCategory(categoryLabel: String?): String = when (categoryLabel) {
        "업무" -> STAT_FOCUS
        "건강" -> STAT_VITALITY
        "학습" -> STAT_INSIGHT
        else -> STAT_BALANCE
    }

    fun progressFor(totalExp: Int): CharacterLevelProgress {
        var level = 1
        var remaining = totalExp.coerceAtLeast(0)
        var next = expForLevel(level)

        while (remaining >= next) {
            remaining -= next
            level += 1
            next = expForLevel(level)
        }

        return CharacterLevelProgress(
            level = level,
            expInLevel = remaining,
            nextLevelExp = next,
            totalExp = totalExp.coerceAtLeast(0),
        )
    }

    private fun expForLevel(level: Int): Int = 100 + ((level - 1).coerceAtLeast(0) * 50)
}
