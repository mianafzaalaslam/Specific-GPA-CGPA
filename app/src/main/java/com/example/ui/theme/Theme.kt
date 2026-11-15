package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme =
  lightColorScheme(
    primary = OxfordNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5F5),
    onPrimaryContainer = OxfordNavy,
    secondary = SlateBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3EBF8),
    onSecondaryContainer = InkPrimary,
    tertiary = AcademicGold,
    onTertiary = Color.White,
    tertiaryContainer = AcademicGoldSoft,
    onTertiaryContainer = Color(0xFF422C00),
    background = PaperBackground,
    onBackground = InkPrimary,
    surface = PaperSurface,
    onSurface = InkPrimary,
    surfaceVariant = PaperSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    surfaceContainerLow = Color(0xFFF9FAFD),
    surfaceContainer = PaperSurface,
    surfaceContainerHigh = Color(0xFFEFF3FA),
    outline = PaperOutline,
    outlineVariant = Color(0xFFE2E7F0),
    error = GradeCrimson,
    onError = Color.White,
    errorContainer = GradeCrimsonContainer,
    onErrorContainer = Color(0xFF690005),
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = IceBluePrimary,
    onPrimary = IceBlueOnPrimary,
    primaryContainer = Color(0xFF193560),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFA7BFE8),
    onSecondary = Color(0xFF132645),
    secondaryContainer = MidnightSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = AmberGoldDark,
    onTertiary = Color(0xFF392600),
    tertiaryContainer = AmberGoldContainerDark,
    onTertiaryContainer = Color(0xFFFFDFA2),
    background = MidnightBackground,
    onBackground = DarkTextPrimary,
    surface = MidnightSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLow = Color(0xFF0F1727),
    surfaceContainer = MidnightSurface,
    surfaceContainerHigh = MidnightSurfaceVariant,
    outline = MidnightOutline,
    outlineVariant = Color(0xFF202D45),
    error = GradeCrimsonDark,
    onError = Color(0xFF5D0006),
    errorContainer = GradeCrimsonContainerDark,
    onErrorContainer = Color(0xFFFFDAD6),
  )

val AppShapes =
  Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun SpecificGpaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
