package com.example.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AcademicGold
import com.example.ui.theme.AcademicGoldSoft
import com.example.ui.theme.AmberGoldContainerDark
import com.example.ui.theme.AmberGoldDark
import com.example.ui.theme.GradeAmber
import com.example.ui.theme.GradeAmberContainer
import com.example.ui.theme.GradeCrimson
import com.example.ui.theme.GradeCrimsonContainer
import com.example.ui.theme.GradeCrimsonContainerDark
import com.example.ui.theme.GradeCrimsonDark
import com.example.ui.theme.GradeEmerald
import com.example.ui.theme.GradeEmeraldContainer
import com.example.ui.theme.GradeEmeraldContainerDark
import com.example.ui.theme.GradeEmeraldDark
import com.example.ui.theme.GradeNeutralBadge
import com.example.ui.theme.GradeNeutralContainer
import com.example.ui.theme.GradeRoyalBlue
import com.example.ui.theme.GradeRoyalBlueContainer
import java.math.BigDecimal
import java.util.Locale

fun formatGpa(value: Double): String = String.format(Locale.US, "%.2f", value)

fun formatCredits(value: Double): String {
  val rounded = BigDecimal.valueOf(value).stripTrailingZeros()
  return if (rounded.scale() <= 0) {
    rounded.toPlainString()
  } else {
    String.format(Locale.US, "%.1f", value)
  }
}

fun formatMarks(value: Double): String {
  val clean = BigDecimal.valueOf(value).setScale(2, java.math.RoundingMode.HALF_UP).stripTrailingZeros()
  return clean.toPlainString()
}

data class GradeVisualStyle(val container: Color, val content: Color)

@Composable
fun rememberGradeVisualStyle(letterGrade: String): GradeVisualStyle {
  val isDark = isSystemInDarkTheme()
  return when (letterGrade) {
    "A+", "A" ->
      if (isDark) GradeVisualStyle(GradeEmeraldContainerDark, GradeEmeraldDark)
      else GradeVisualStyle(GradeEmeraldContainer, GradeEmerald)
    "B" ->
      if (isDark) GradeVisualStyle(Color(0xFF1A3363), Color(0xFF93B8FF))
      else GradeVisualStyle(GradeRoyalBlueContainer, GradeRoyalBlue)
    "C" ->
      if (isDark) GradeVisualStyle(AmberGoldContainerDark, AmberGoldDark)
      else GradeVisualStyle(AcademicGoldSoft, AcademicGold)
    "D" ->
      if (isDark) GradeVisualStyle(Color(0xFF422A0E), Color(0xFFF6B76B))
      else GradeVisualStyle(GradeAmberContainer, GradeAmber)
    "F", "I" ->
      if (isDark) GradeVisualStyle(GradeCrimsonContainerDark, GradeCrimsonDark)
      else GradeVisualStyle(GradeCrimsonContainer, GradeCrimson)
    else ->
      if (isDark) GradeVisualStyle(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
      else GradeVisualStyle(GradeNeutralContainer, GradeNeutralBadge)
  }
}
