package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CourseEntity
import com.example.data.SemesterEntity
import com.example.domain.CourseEvaluation
import com.example.domain.CourseStatus
import com.example.domain.MarksEntryMode
import com.example.domain.UniversityGradingEngine
import com.example.ui.GpaUiState
import com.example.ui.components.AddSemesterDialog
import com.example.ui.components.CourseEditorSheet
import com.example.ui.components.GpaArcGauge
import com.example.ui.components.formatCredits
import com.example.ui.components.formatGpa
import com.example.ui.components.formatMarks
import com.example.ui.components.rememberGradeVisualStyle
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.OxfordNavy
import com.example.ui.theme.OxfordNavyLight

@Composable
fun GpaCalculatorScreen(
  uiState: GpaUiState,
  onSelectSemester: (Long) -> Unit,
  onAddSemester: (title: String, isSummaryOnly: Boolean, gpa: Double, credits: Double) -> Unit,
  onUpdateSemester: (SemesterEntity, String, Double, Double) -> Unit,
  onSaveCourse: (CourseEntity) -> Unit,
  onDeleteCourse: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  var editingCourse by remember { mutableStateOf<CourseEntity?>(null) }
  var showCourseSheet by remember { mutableStateOf(false) }
  var showNewSemesterDialog by remember { mutableStateOf(false) }
  var editingSemesterEntity by remember { mutableStateOf<SemesterEntity?>(null) }

  val activeSemester = uiState.activeSemesterEntity
  val activeSummary = uiState.activeSemesterSummary
  val courseBasedSemesters =
    uiState.semestersWithCourses.filter { !it.semester.isSummaryOnly }.map { it.semester }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background,
    floatingActionButton = {
      if (activeSemester != null && !activeSemester.isSummaryOnly) {
        FloatingActionButton(
          onClick = {
            editingCourse = null
            showCourseSheet = true
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("add_course_fab"),
        ) {
          Icon(Icons.Rounded.Add, contentDescription = "Add course")
        }
      }
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // App Identity Header + Cumulative CGPA quick pill
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Specific GPA/CGPA Calculator",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "Clause 14 Scale · 4.00 Max · 50% Pass",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.tertiaryContainer,
            ) {
              Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.End,
              ) {
                Text(
                  text = "CGPA",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Text(
                  text = formatGpa(uiState.cgpaSummary.cgpa),
                  fontFamily = JetBrainsMonoFamily,
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
              }
            }
          }
        }

        // Semester Switcher Row
        item {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            items(courseBasedSemesters, key = { it.id }) { semester ->
              FilterChip(
                selected = semester.id == activeSemester?.id,
                onClick = { onSelectSemester(semester.id) },
                label = { Text(semester.title) },
                modifier = Modifier.testTag("semester_chip_${semester.id}"),
              )
            }
            item {
              AssistChip(
                onClick = { showNewSemesterDialog = true },
                label = { Text("New Semester") },
                leadingIcon = {
                  Icon(
                    Icons.Rounded.Add,
                    contentDescription = "New semester",
                    modifier = Modifier.size(18.dp),
                  )
                },
                modifier = Modifier.testTag("new_semester_chip"),
              )
            }
          }
        }

        // Hero GPA Card
        if (activeSummary != null && activeSemester != null) {
          item {
            Box(
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(24.dp))
                  .background(
                    Brush.linearGradient(colors = listOf(OxfordNavy, OxfordNavyLight))
                  )
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
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = activeSemester.title,
                      style = MaterialTheme.typography.titleMedium,
                      color = Color.White,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.weight(1f, fill = false),
                    )
                    IconButton(
                      onClick = { editingSemesterEntity = activeSemester },
                      modifier = Modifier.size(36.dp).testTag("rename_semester_button"),
                    ) {
                      Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Rename semester",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }

                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.14f),
                  ) {
                    Text(
                      text = UniversityGradingEngine.standingLabel(activeSummary.gpa),
                      style = MaterialTheme.typography.labelMedium,
                      color = Color(0xFFF6D07A),
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    HeroMetric(
                      label = "GPA Credits",
                      value = formatCredits(activeSummary.gpaCreditHours),
                    )
                    HeroMetric(
                      label = "Earned Cr",
                      value = formatCredits(activeSummary.earnedCreditHours),
                    )
                    HeroMetric(
                      label = "Quality Pts",
                      value = formatGpa(activeSummary.totalQualityPoints),
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                GpaArcGauge(
                  gpa = activeSummary.gpa,
                  label = "SGPA / 4.00",
                  size = 122.dp,
                )
              }
            }
          }
        }

        // Course List Header
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Courses (${uiState.activeCourses.size})",
              style = MaterialTheme.typography.titleMedium,
            )
            if (activeSummary != null && activeSummary.courseCount > activeSummary.gradedCourseCount) {
              Text(
                text = "${activeSummary.courseCount - activeSummary.gradedCourseCount} excluded (RL/W/R/EX)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        // Empty State or Courses
        if (uiState.activeCourses.isEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(20.dp),
              colors =
                CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Surface(
                  shape = RoundedCornerShape(16.dp),
                  color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                  Icon(
                    Icons.Rounded.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(14.dp).size(32.dp),
                  )
                }
                Text(
                  text = "No courses in ${activeSemester?.title ?: "this semester"}",
                  style = MaterialTheme.typography.titleMedium,
                )
                Text(
                  text =
                    "Add courses using Direct Marks %, Mid/Sessional/Final breakdown (rounded up per Clause 14.3), or special codes (RL, W, R, I, EX).",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                  onClick = {
                    editingCourse = null
                    showCourseSheet = true
                  },
                  modifier = Modifier.height(48.dp).testTag("empty_add_course_button"),
                ) {
                  Icon(Icons.Rounded.Add, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Add First Course")
                }
              }
            }
          }
        } else {
          val evaluationsById =
            activeSummary?.evaluations?.associateBy { it.courseId } ?: emptyMap()

          items(uiState.activeCourses, key = { it.id }) { course ->
            val evaluation = evaluationsById[course.id]
            if (evaluation != null) {
              CourseCard(
                course = course,
                evaluation = evaluation,
                onEdit = {
                  editingCourse = course
                  showCourseSheet = true
                },
                onDelete = { onDeleteCourse(course.id) },
              )
            }
          }
        }
      }
    }
  }

  if (showCourseSheet && activeSemester != null) {
    CourseEditorSheet(
      semesterId = activeSemester.id,
      existingCourse = editingCourse,
      nextCourseIndex = uiState.activeCourses.size + 1,
      onDismiss = {
        showCourseSheet = false
        editingCourse = null
      },
      onSave = { courseEntity ->
        onSaveCourse(courseEntity)
        showCourseSheet = false
        editingCourse = null
      },
    )
  }

  if (showNewSemesterDialog) {
    AddSemesterDialog(
      defaultTitle = "Semester ${uiState.semestersWithCourses.size + 1}",
      onDismiss = { showNewSemesterDialog = false },
      onConfirmNew = { title, isSummary, gpa, credits ->
        onAddSemester(title, isSummary, gpa, credits)
        showNewSemesterDialog = false
      },
    )
  }

  if (editingSemesterEntity != null) {
    AddSemesterDialog(
      defaultTitle = editingSemesterEntity!!.title,
      existingSemester = editingSemesterEntity,
      onDismiss = { editingSemesterEntity = null },
      onConfirmNew = { _, _, _, _ -> },
      onConfirmUpdate = { sem, title, gpa, credits ->
        onUpdateSemester(sem, title, gpa, credits)
        editingSemesterEntity = null
      },
    )
  }
}

@Composable
private fun HeroMetric(label: String, value: String) {
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
private fun CourseCard(
  course: CourseEntity,
  evaluation: CourseEvaluation,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  val gradeColors = rememberGradeVisualStyle(evaluation.letterGrade)
  val isExamBreakdown =
    evaluation.status == CourseStatus.GRADED &&
      MarksEntryMode.fromNameOrDefault(course.entryMode) == MarksEntryMode.EXAM_BREAKDOWN

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onEdit)
        .testTag("course_card_${course.id}"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Grade badge
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = gradeColors.container,
        modifier = Modifier.width(62.dp),
      ) {
        Column(
          modifier = Modifier.padding(vertical = 10.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            text = evaluation.letterGrade,
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.titleLarge,
            color = gradeColors.content,
          )
          Text(
            text = evaluation.gradePoint?.let { formatGpa(it) } ?: "----",
            fontFamily = JetBrainsMonoFamily,
            style = MaterialTheme.typography.labelSmall,
            color = gradeColors.content,
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = evaluation.courseName,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
          ) {
            Text(
              text = "${formatCredits(evaluation.creditHours)} Cr",
              fontFamily = JetBrainsMonoFamily,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
          }

          if (evaluation.status == CourseStatus.GRADED && evaluation.rawMarks != null) {
            val marksDisplay =
              if (evaluation.wasRoundedUp) {
                "${formatMarks(evaluation.rawMarks)}% → ${evaluation.roundedMarks}%"
              } else {
                "${evaluation.roundedMarks}%"
              }
            Text(
              text = marksDisplay,
              fontFamily = JetBrainsMonoFamily,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            if (evaluation.wasRoundedUp) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    Icons.Rounded.ArrowUpward,
                    contentDescription = "Rounded up by Clause 14.3",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(11.dp),
                  )
                  Text(
                    text = "14.3",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                  )
                }
              }
            }
          } else {
            Text(
              text = evaluation.status.displayName,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        if (isExamBreakdown) {
          Text(
            text =
              "Mid ${formatMarks(course.midMarks ?: 0.0)} + Sess ${formatMarks(course.sessionalMarks ?: 0.0)} + Final ${formatMarks(course.finalMarks ?: 0.0)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          Text(
            text =
              if (evaluation.countsInGpa) {
                "Quality Points: ${formatGpa(evaluation.qualityPoints)}"
              } else {
                "Excluded from GPA denominator"
              },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(48.dp).testTag("delete_course_${course.id}"),
      ) {
        Icon(
          Icons.Outlined.DeleteOutline,
          contentDescription = "Delete course",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
