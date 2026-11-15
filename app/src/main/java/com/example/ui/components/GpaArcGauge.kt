package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JetBrainsMonoFamily
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GpaArcGauge(
  gpa: Double,
  label: String,
  modifier: Modifier = Modifier,
  size: Dp = 128.dp,
  trackColor: Color = Color.White.copy(alpha = 0.16f),
  accentStart: Color = Color(0xFFF5C45E),
  accentEnd: Color = Color(0xFF5AD4A5),
) {
  val targetFraction = (gpa / 4.0).toFloat().coerceIn(0f, 1f)
  val animatedFraction by
    animateFloatAsState(
      targetValue = targetFraction,
      animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
      label = "gpa_gauge_fraction",
    )

  Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.matchParentSize()) {
      val strokeWidth = 11.dp.toPx()
      val inset = strokeWidth / 2f + 4.dp.toPx()
      val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)
      val topLeft = Offset(inset, inset)
      val startAngle = 140f
      val sweepTotal = 260f

      // Background track
      drawArc(
        color = trackColor,
        startAngle = startAngle,
        sweepAngle = sweepTotal,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
      )

      // 2.00 GPA passing marker tick
      val passAngleRad = Math.toRadians((startAngle + sweepTotal * 0.5f).toDouble())
      val cx = this.size.width / 2f
      val cy = this.size.height / 2f
      val radius = arcSize.width / 2f
      val tickInner = radius - strokeWidth * 0.6f
      val tickOuter = radius + strokeWidth * 0.6f
      drawLine(
        color = Color.White.copy(alpha = 0.42f),
        start =
          Offset(
            (cx + tickInner * cos(passAngleRad)).toFloat(),
            (cy + tickInner * sin(passAngleRad)).toFloat(),
          ),
        end =
          Offset(
            (cx + tickOuter * cos(passAngleRad)).toFloat(),
            (cy + tickOuter * sin(passAngleRad)).toFloat(),
          ),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )

      // Active value arc
      if (animatedFraction > 0.005f) {
        drawArc(
          brush = Brush.sweepGradient(listOf(accentStart, accentEnd, accentStart)),
          startAngle = startAngle,
          sweepAngle = sweepTotal * animatedFraction,
          useCenter = false,
          topLeft = topLeft,
          size = arcSize,
          style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
      }
    }

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = formatGpa(gpa),
        fontFamily = JetBrainsMonoFamily,
        fontSize = 28.sp,
        color = Color.White,
        style = MaterialTheme.typography.displayMedium,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.78f),
      )
    }
  }
}
