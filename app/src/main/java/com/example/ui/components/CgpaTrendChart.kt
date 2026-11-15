package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.SemesterSummary

@Composable
fun CgpaTrendChart(
  semesters: List<SemesterSummary>,
  priorEnabled: Boolean,
  priorCgpa: Double,
  priorCredits: Double,
  modifier: Modifier = Modifier,
) {
  val chartSemesters = semesters.filter { it.includedInCgpa && it.gpaCreditHours > 0.0 }
  if (chartSemesters.isEmpty()) return

  val primaryColor = MaterialTheme.colorScheme.primary
  val barColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
  val goldColor = MaterialTheme.colorScheme.tertiary
  val gridColor = MaterialTheme.colorScheme.outlineVariant
  val surfaceColor = MaterialTheme.colorScheme.surface

  // Compute running CGPA after each included semester
  val runningCgpaPoints = buildList {
    var cumQp = if (priorEnabled && priorCredits > 0.0) priorCgpa * priorCredits else 0.0
    var cumCr = if (priorEnabled && priorCredits > 0.0) priorCredits else 0.0
    for (sem in chartSemesters) {
      cumQp += sem.totalQualityPoints
      cumCr += sem.gpaCreditHours
      add(if (cumCr > 0.0) (cumQp / cumCr).coerceIn(0.0, 4.0) else 0.0)
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Semester Trajectory",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LegendDot(color = primaryColor.copy(alpha = 0.4f), label = "SGPA")
        LegendDot(color = goldColor, label = "CGPA")
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Canvas(
      modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 4.dp)
    ) {
      val leftPadding = 8.dp.toPx()
      val rightPadding = 8.dp.toPx()
      val topPadding = 12.dp.toPx()
      val bottomPadding = 12.dp.toPx()
      val plotW = (size.width - leftPadding - rightPadding).coerceAtLeast(1f)
      val plotH = (size.height - topPadding - bottomPadding).coerceAtLeast(1f)

      fun yForGpa(gpa: Double): Float {
        val ratio = (gpa / 4.0).toFloat().coerceIn(0f, 1f)
        return topPadding + plotH * (1f - ratio)
      }

      // Horizontal reference lines for 4.0, 3.0, 2.0 (Pass)
      listOf(4.0, 3.0, 2.0).forEach { ref ->
        val y = yForGpa(ref)
        drawLine(
          color = gridColor,
          start = Offset(leftPadding, y),
          end = Offset(leftPadding + plotW, y),
          strokeWidth = 1.dp.toPx(),
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
        )
      }

      val count = chartSemesters.size
      val slotW = plotW / count.toFloat()
      val barW = (slotW * 0.42f).coerceIn(14.dp.toPx(), 38.dp.toPx())

      val linePath = Path()
      val pointCoords = mutableListOf<Offset>()

      chartSemesters.forEachIndexed { idx, sem ->
        val centerX = leftPadding + slotW * idx + slotW / 2f
        val barTop = yForGpa(sem.gpa)
        val barBottom = topPadding + plotH
        val barHeight = (barBottom - barTop).coerceAtLeast(4.dp.toPx())

        drawRoundRect(
          color = barColor,
          topLeft = Offset(centerX - barW / 2f, barTop),
          size = Size(barW, barHeight),
          cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        )

        val cgpaY = yForGpa(runningCgpaPoints[idx])
        val pt = Offset(centerX, cgpaY)
        pointCoords.add(pt)
        if (idx == 0) linePath.moveTo(pt.x, pt.y) else linePath.lineTo(pt.x, pt.y)
      }

      if (pointCoords.size >= 2) {
        drawPath(
          path = linePath,
          color = goldColor,
          style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
      }

      pointCoords.forEach { pt ->
        drawCircle(color = goldColor, radius = 5.dp.toPx(), center = pt)
        drawCircle(color = surfaceColor, radius = 2.5.dp.toPx(), center = pt)
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
      chartSemesters.forEach { sem ->
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = sem.title.replace("Semester ", "S"),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = formatGpa(sem.gpa),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
  }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = color) {}
    Spacer(modifier = Modifier.width(5.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
