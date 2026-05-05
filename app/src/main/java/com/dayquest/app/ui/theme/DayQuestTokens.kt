package com.dayquest.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val DayQuestLightColors = lightColorScheme(
    primary = Color(0xFF705836),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF8A704C),
    onPrimaryContainer = Color(0xFFFFF8EF),
    secondary = Color(0xFF5C614D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE0E5CC),
    onSecondaryContainer = Color(0xFF303522),
    tertiary = Color(0xFF356363),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD5ECEB),
    onTertiaryContainer = Color(0xFF103838),
    background = Color(0xFFFFF9ED),
    onBackground = Color(0xFF1F1C0B),
    surface = Color(0xFFFCF3D8),
    onSurface = Color(0xFF1F1C0B),
    surfaceVariant = Color(0xFFEBE2C8),
    onSurfaceVariant = Color(0xFF4E453C),
    outline = Color(0xFF7F756A),
    outlineVariant = Color(0xFFD1C5B8),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
)

internal val DayQuestDarkColors = darkColorScheme(
    primary = Color(0xFFB8936B),
    onPrimary = Color(0xFF2C1E10),
    primaryContainer = Color(0xFF705836),
    onPrimaryContainer = Color(0xFFF7E8CF),
    secondary = Color(0xFFA8AF95),
    onSecondary = Color(0xFF1F2414),
    secondaryContainer = Color(0xFF3A4030),
    onSecondaryContainer = Color(0xFFE1E6CF),
    tertiary = Color(0xFF7AB1B1),
    onTertiary = Color(0xFF0E3131),
    tertiaryContainer = Color(0xFF234848),
    onTertiaryContainer = Color(0xFFD5EEEE),
    background = Color(0xFF17140F),
    onBackground = Color(0xFFECE4D7),
    surface = Color(0xFF211C17),
    onSurface = Color(0xFFF4EBDD),
    surfaceVariant = Color(0xFF2D2721),
    onSurfaceVariant = Color(0xFFC7B9A7),
    outline = Color(0xFF7C6C5B),
    outlineVariant = Color(0xFF473F37),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

internal val DayQuestTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp,
    ),
)

internal val DayQuestShapes = Shapes(
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(34.dp),
)

internal val DayQuestTaskAccentCompleted = Color(0xFF7B8A63)
internal val DayQuestTaskAccentOverdue = Color(0xFFA68A64)
internal val DayQuestTaskAccentMain = Color(0xFF705836)
internal val DayQuestTaskAccentRare = Color(0xFF356363)
internal val DayQuestTaskAccentNormal = Color(0xFF8B8277)
