package com.dayquest.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterGrowthRulesTest {
    @Test
    fun progressForLevelsUpAcrossIncreasingThresholds() {
        val progress = CharacterGrowthRules.progressFor(260)

        assertEquals(3, progress.level)
        assertEquals(10, progress.expInLevel)
        assertEquals(200, progress.nextLevelExp)
        assertEquals(260, progress.totalExp)
    }

    @Test
    fun statForCategoryMapsDailyTodoRewardToCharacterStat() {
        assertEquals(CharacterGrowthRules.STAT_FOCUS, CharacterGrowthRules.statForCategory("업무"))
        assertEquals(CharacterGrowthRules.STAT_VITALITY, CharacterGrowthRules.statForCategory("건강"))
        assertEquals(CharacterGrowthRules.STAT_INSIGHT, CharacterGrowthRules.statForCategory("학습"))
        assertEquals(CharacterGrowthRules.STAT_BALANCE, CharacterGrowthRules.statForCategory("개인"))
    }
}
