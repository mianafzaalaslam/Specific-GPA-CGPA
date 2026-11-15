package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.CourseStatus
import com.example.domain.GradeCategory
import com.example.domain.GradeScaleEntry
import com.example.domain.UniversityGradingEngine
import com.example.ui.components.formatGpa
import com.example.ui.components.formatMarks
import com.example.ui.components.rememberGradeVisualStyle
import com.example.ui.theme.JetBrainsMonoFamily

private enum class TableFilter(val label: String) {
  ALL("All"),
  A("A+ / A"),
  B("B"),
  C("C"),
  D("D"),
  SPECIAL("F & Special"),
}

@Composable
fun GradingSchemeScreen(modifier: Modifier = Modifier) {
  var midInput by remember { mutableStateOf("19.0") }
  var sessInput by remember { mutableStateOf("10.3") }
  var finalInput by remember { mutableStateOf("40.1") }
  var selectedFilter by remember { mutableStateOf(TableFilter.ALL) }

  val rawTotal =
    UniversityGradingEngine.computeExamTotalPercentage(
      midTerm = midInput.toDoubleOrNull() ?: 0.0,
      sessional = sessInput.toDoubleOrNull() ?: 0.0,
      finalTerm = finalInput.toDoubleOrNull() ?: 0.0,
      maxMarks = 100.0,
    )
  val simEval =
    UniversityGradingEngine.evaluateCourse(
      courseId = 0L,
      courseName = "Simulator",
      creditHours = 3.0,
      status = CourseStatus.GRADED,
      rawPercentage = rawTotal,
    )

  val filteredRows =
    remember(selectedFilter) {
      UniversityGradingEngine.officialGradingTable.filter { row ->
        when (selectedFilter) {
          TableFilter.ALL -> true
          TableFilter.A -> row.category == GradeCategory.A_GROUP
          TableFilter.B -> row.category == GradeCategory.B_GROUP
          TableFilter.C -> row.category == GradeCategory.C_GROUP
          TableFilter.D -> row.category == GradeCategory.D_GROUP
          TableFilter.SPECIAL ->
            row.category == GradeCategory.FAIL || row.category == GradeCategory.SPECIAL
        }
      }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Column {
          Text(
            text = "Section 14 Grading System",
            style = MaterialTheme.typography.headlineMedium,
          )
          Text(
            text = "Scale of 4.00 · Passing Marks = 50% (D Grade)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Clause 14.3 Rounding Lab
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
              ) {
                Icon(
                  Icons.Rounded.Calculate,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onPrimaryContainer,
                  modifier = Modifier.padding(8.dp),
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Clause 14.3 Rounding Simulator",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text = "Fractions count as 1 mark (e.g., 60.1 & 60.9 → 61)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = midInput,
                onValueChange = {
                  if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) midInput = it
                },
                label = { Text("Mid (30)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f).testTag("sim_mid_input"),
              )
              OutlinedTextField(
                value = sessInput,
                onValueChange = {
                  if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) sessInput = it
                },
                label = { Text("Sess (20)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f).testTag("sim_sess_input"),
              )
              OutlinedTextField(
                value = finalInput,
                onValueChange = {
                  if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) finalInput = it
                },
                label = { Text("Final (50)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f).testTag("sim_final_input"),
              )
            }

            val simStyle = rememberGradeVisualStyle(simEval.letterGrade)
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceContainerHigh,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                    text = "Raw Total: ${formatMarks(rawTotal)} / 100",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Text(
                    text = "Rounded Score: ${simEval.roundedMarks ?: 0}",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleMedium,
                  )
                }
                Surface(shape = RoundedCornerShape(12.dp), color = simStyle.container) {
                  Text(
                    text = "${simEval.letterGrade} · ${formatGpa(simEval.gradePoint ?: 0.0)}",
                    fontFamily = JetBrainsMonoFamily,
                    style = MaterialTheme.typography.titleSmall,
                    color = simStyle.content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                  )
                }
              }
            }

            OutlinedButton(
              onClick = {
                midInput = "19.0"
                sessInput = "10.3"
                finalInput = "40.1"
              },
              modifier = Modifier.fillMaxWidth().height(44.dp).testTag("reset_clause_14_3_example"),
            ) {
              Icon(Icons.Rounded.AutoFixHigh, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Reset to Official Example (69.4 → 70)")
            }
          }
        }
      }

      // Clauses Summary Card
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Rounded.Rule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Regulation Highlights", style = MaterialTheme.typography.titleMedium)
            }
            Text(
              text = "• 14.1: Grading is performed strictly on a scale of 4.00.",
              style = MaterialTheme.typography.bodySmall,
            )
            Text(
              text = "• 14.2: Passing marks for any course is 50% (D grade = 2.00 GP).",
              style = MaterialTheme.typography.bodySmall,
            )
            Text(
              text = "• 14.3: Any fractional mark counts as 1 full mark (60.1 → 61, 69.4 → 70).",
              style = MaterialTheme.typography.bodySmall,
            )
            Text(
              text = "• 14.4: GPA = Σ(Course GP × Credit Hours) ÷ Σ(Credit Hours).",
              style = MaterialTheme.typography.bodySmall,
            )
          }
        }
      }

      // Filter Chips for Grading Table
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(TableFilter.entries) { filter ->
            FilterChip(
              selected = selectedFilter == filter,
              onClick = { selectedFilter = filter },
              label = { Text(filter.label) },
              modifier = Modifier.testTag("scheme_filter_${filter.name.lowercase()}"),
            )
          }
        }
      }

      // Table Header
      item {
        Surface(
          shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Marks %",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.weight(1.3f),
            )
            Text(
              text = "Grade Point",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.weight(1f),
            )
            Text(
              text = "Letter Grade",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.weight(0.8f),
            )
          }
        }
      }

      items(filteredRows, key = { "${it.marksLabel}_${it.letterGrade}" }) { row ->
        GradingTableRow(row = row)
      }
    }
  }
}

@Composable
private fun GradingTableRow(row: GradeScaleEntry) {
  val style = rememberGradeVisualStyle(row.letterGrade)
  Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = row.marksLabel,
          fontFamily = JetBrainsMonoFamily,
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1.3f),
        )
        Text(
          text = row.gradePointDisplay,
          fontFamily = JetBrainsMonoFamily,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(1f),
        )
        Box(modifier = Modifier.weight(0.8f)) {
          Surface(shape = RoundedCornerShape(8.dp), color = style.container) {
            Text(
              text = row.letterGrade,
              fontFamily = JetBrainsMonoFamily,
              style = MaterialTheme.typography.labelMedium,
              color = style.content,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
          }
        }
      }
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
  }
}
