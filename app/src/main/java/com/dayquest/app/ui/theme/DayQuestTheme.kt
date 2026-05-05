package com.dayquest.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DayQuestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DayQuestDarkColors else DayQuestLightColors,
        typography = DayQuestTypography,
        shapes = DayQuestShapes,
        content = content,
    )
}
