package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SemesterEntity
import com.example.domain.SemesterSummary
import com.example.domain.UniversityGradingEngine
import com.example.ui.GpaUiState
import com.example.ui.components.AddSemesterDialog
import com.example.ui.components.CgpaTrendChart
import com.example.ui.components.GpaArcGauge
import com.example.ui.components.PriorAcademicDialog
import com.example.ui.components.formatCredits
import com.example.ui.components.formatGpa
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.OxfordNavy
import com.example.ui.theme.OxfordNavyLight

@Composable
fun CgpaDashboardScreen(
  uiState: GpaUiState,
  onOpenSemesterInGpa: (Long) -> Unit,
  onAddSemester: (title: String, isSummaryOnly: Boolean, gpa: Double, credits: Double) -> Unit,
  onUpdateSemester: (SemesterEntity, String, Double, Double) -> Unit,
  onToggleSemesterIncluded: (SemesterEntity) -> Unit,
  onDeleteSemester: (SemesterEntity) -> Unit,
  onSavePriorAcademic: (enabled: Boolean, cgpa: Double, credits: Double) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showAddSemesterDialog by remember { mutableStateOf(false) }
  var editingSemester by remember { mutableStateOf<SemesterEntity?>(null) }
  var showPriorDialog by remember { mutableStateOf(false) }
  var showPlanner by remember { mutableStateOf(false) }
  var targetCgpaInput by remember { mutableStateOf("3.50") }
  var upcomingCreditsInput by remember { mutableStateOf("18") }

  val cgpaSummary = uiState.cgpaSummary
  val prior = uiState.priorAcademic

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "Cumulative CGPA",
              style = MaterialTheme.typography.headlineMedium,
            )
            Text(
              text = "Weighted across all included semesters (Clause 14.4)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          FilledTonalButton(
            onClick = { showAddSemesterDialog = true },
            modifier = Modifier.height(44.dp).testTag("cgpa_add_semester_button"),
          ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Semester")
          }
        }
      }

      // Hero CGPA Card
      item {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(24.dp))
              .background(Brush.linearGradient(listOf(OxfordNavy, OxfordNavyLight)))
              .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.14f),
              ) {
                Text(
                  text = UniversityGradingEngine.standingLabel(cgpaSummary.cgpa),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color(0xFFF6D07A),
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
              }

              Text(
                text =
                  buildString {
                    append("${cgpaSummary.includedSemesterCount} semester(s)")
                    if (cgpaSummary.hasPriorBaseline) append(" + prior record")
                  },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
              )

              Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CgpaHeroMetric(
                  label = "Total Credits",
                  value = formatCredits(cgpaSummary.totalCreditHours),
                )
                CgpaHeroMetric(
                  label = "Earned Cr",
                  value = formatCredits(cgpaSummary.totalEarnedCreditHours),
                )
                CgpaHeroMetric(
                  label = "Quality Pts",
                  value = formatGpa(cgpaSummary.totalQualityPoints),
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            GpaArcGauge(
              gpa = cgpaSummary.cgpa,
              label = "CGPA / 4.00",
              size = 126.dp,
            )
          }
        }
      }

      // Prior CGPA Baseline Card
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
              ) {
                Icon(
                  Icons.Rounded.HistoryEdu,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.padding(10.dp).size(22.dp),
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Prior CGPA Baseline",
                  style = MaterialTheme.typography.titleSmall,
                )
                Text(
                  text =
                    if (prior.enabled && prior.priorCreditHours > 0.0) {
                      "${formatGpa(prior.priorCgpa)} CGPA · ${formatCredits(prior.priorCreditHours)} Cr included"
                    } else {
                      "Optional: combine past completed credits"
                    },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
            OutlinedButton(
              onClick = { showPriorDialog = true },
              modifier = Modifier.height(40.dp).testTag("edit_prior_button"),
            ) {
              Text(if (prior.enabled) "Edit" else "Set")
            }
          }
        }
      }

      // Trend Chart Card
      val hasChartData =
        uiState.semesterSummaries.any { it.includedInCgpa && it.gpaCreditHours > 0.0 }
      if (hasChartData) {
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
          ) {
            CgpaTrendChart(
              semesters = uiState.semesterSummaries,
              priorEnabled = prior.enabled,
              priorCgpa = prior.priorCgpa,
              priorCredits = prior.priorCreditHours,
              modifier = Modifier.padding(16.dp),
            )
          }
        }
      }

      // Target CGPA Planner Card
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(10.dp))
                  .clickable { showPlanner = !showPlanner }
                  .padding(vertical = 4.dp)
                  .testTag("toggle_target_planner"),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.tertiaryContainer,
                ) {
                  Icon(
                    Icons.Rounded.TrackChanges,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(8.dp).size(20.dp),
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Target CGPA Planner",
                    style = MaterialTheme.typography.titleSmall,
                  )
                  Text(
                    text = "Find required SGPA for upcoming credits",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              Icon(
                if (showPlanner) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = "Toggle target planner",
              )
            }

            AnimatedVisibility(visible = showPlanner) {
              val targetG = (targetCgpaInput.toDoubleOrNull() ?: 3.50).coerceIn(0.0, 4.0)
              val nextCr = (upcomingCreditsInput.toDoubleOrNull() ?: 18.0).coerceAtLeast(0.0)
              val plan =
                UniversityGradingEngine.calculateTargetPlan(
                  currentQualityPoints = cgpaSummary.totalQualityPoints,
                  currentCreditHours = cgpaSummary.totalCreditHours,
                  upcomingCreditHours = nextCr,
                  targetCgpa = targetG,
                )

              Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                  OutlinedTextField(
                    value = targetCgpaInput,
                    onValueChange = {
                      if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        targetCgpaInput = it
                      }
                    },
                    label = { Text("Target CGPA") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).testTag("target_cgpa_input"),
                  )
                  OutlinedTextField(
                    value = upcomingCreditsInput,
                    onValueChange = {
                      if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                        upcomingCreditsInput = it
                      }
                    },
                    label = { Text("Next Credits") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f).testTag("upcoming_credits_input"),
                  )
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color =
                    if (plan.isReachable) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.errorContainer,
                  modifier = Modifier.fillMaxWidth(),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text =
                        if (plan.isReachable) {
                          "Required SGPA: ${formatGpa(plan.requiredNextGpa)} / 4.00"
                        } else {
                          "Needs ${formatGpa(plan.requiredNextGpa)} (above 4.00 cap)"
                        },
                      fontFamily = JetBrainsMonoFamily,
                      style = MaterialTheme.typography.titleSmall,
                      color =
                        if (plan.isReachable) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                      text =
                        "With ${formatCredits(nextCr)} upcoming Cr, your CGPA range is ${formatGpa(plan.projectedMinCgpa)} – ${formatGpa(plan.projectedMaxCgpa)}.",
                      style = MaterialTheme.typography.bodySmall,
                      color =
                        if (plan.isReachable) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer,
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Semesters List Header
      item {
        Text(
          text = "Semesters (${uiState.semestersWithCourses.size})",
          style = MaterialTheme.typography.titleMedium,
        )
      }

      val summaryById = uiState.semesterSummaries.associateBy { it.semesterId }

      items(uiState.semestersWithCourses, key = { it.semester.id }) { pair ->
        val summary = summaryById[pair.semester.id]
        if (summary != null) {
          SemesterTranscriptCard(
            semester = pair.semester,
            summary = summary,
            onOpen = {
              if (pair.semester.isSummaryOnly) {
                editingSemester = pair.semester
              } else {
                onOpenSemesterInGpa(pair.semester.id)
              }
            },
            onEdit = { editingSemester = pair.semester },
            onToggleIncluded = { onToggleSemesterIncluded(pair.semester) },
            onDelete = { onDeleteSemester(pair.semester) },
          )
        }
      }
    }
  }

  if (showAddSemesterDialog) {
    AddSemesterDialog(
      defaultTitle = "Semester ${uiState.semestersWithCourses.size + 1}",
      onDismiss = { showAddSemesterDialog = false },
      onConfirmNew = { title, isSummary, gpa, credits ->
        onAddSemester(title, isSummary, gpa, credits)
        showAddSemesterDialog = false
      },
    )
  }

  if (editingSemester != null) {
    AddSemesterDialog(
      defaultTitle = editingSemester!!.title,
      existingSemester = editingSemester,
      onDismiss = { editingSemester = null },
      onConfirmNew = { _, _, _, _ -> },
      onConfirmUpdate = { sem, title, gpa, credits ->
        onUpdateSemester(sem, title, gpa, credits)
        editingSemester = null
      },
    )
  }

  if (showPriorDialog) {
    PriorAcademicDialog(
      prior = uiState.priorAcademic,
      onDismiss = { showPriorDialog = false },
      onSave = { enabled, cgpa, credits ->
        onSavePriorAcademic(enabled, cgpa, credits)
        showPriorDialog = false
      },
    )
  }
}

@Composable
private fun CgpaHeroMetric(label: String, value: String) {
  Column {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color.White.copy(alpha = 0.72f),
    )
    Text(
      text = value,
      fontFamily = JetBrainsMonoFamily,
      style = MaterialTheme.typography.titleMedium,
      color = Color.White,
    )
  }
}

@Composable
private fun SemesterTranscriptCard(
  semester: SemesterEntity,
  summary: SemesterSummary,
  onOpen: () -> Unit,
  onEdit: () -> Unit,
  onToggleIncluded: () -> Unit,
  onDelete: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (semester.includedInCgpa) MaterialTheme.colorScheme.surface
          else MaterialTheme.colorScheme.surfaceContainerLow
      ),
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onOpen)
        .testTag("semester_transcript_card_${semester.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Surface(
        shape = RoundedCornerShape(14.dp),
        color =
          if (semester.includedInCgpa) MaterialTheme.colorScheme.primaryContainer
          else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(72.dp),
      ) {
        Column(
          modifier = Modifier.padding(vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = formatGpa(summary.gpa),
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.titleMedium,
            color =
              if (semester.includedInCgpa) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "SGPA",
            style = MaterialTheme.typography.labelSmall,
            color =
              if (semester.includedInCgpa) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = semester.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
          )
          if (semester.isSummaryOnly) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Text(
                text = "Summary",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
        }
        Text(
          text =
            if (semester.isSummaryOnly) {
              "${formatCredits(summary.gpaCreditHours)} Cr · ${formatGpa(summary.totalQualityPoints)} Pts"
            } else {
              "${summary.courseCount} courses · ${formatCredits(summary.gpaCreditHours)} GPA Cr"
            },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Switch(
        checked = semester.includedInCgpa,
        onCheckedChange = { onToggleIncluded() },
        modifier = Modifier.testTag("toggle_semester_included_${semester.id}"),
      )
      IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
        Icon(
          Icons.Outlined.Edit,
          contentDescription = "Edit semester",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(48.dp).testTag("delete_semester_${semester.id}"),
      ) {
        Icon(
          Icons.Outlined.DeleteOutline,
          contentDescription = "Delete semester",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
