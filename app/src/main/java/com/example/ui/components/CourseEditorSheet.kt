package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CourseEntity
import com.example.domain.CourseStatus
import com.example.domain.MarksEntryMode
import com.example.domain.UniversityGradingEngine
import com.example.ui.theme.JetBrainsMonoFamily

private enum class SheetInputTab(val title: String) {
  DIRECT("Marks %"),
  BREAKDOWN("Mid / Sess / Final"),
  SPECIAL("Special"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CourseEditorSheet(
  semesterId: Long,
  existingCourse: CourseEntity?,
  nextCourseIndex: Int,
  onDismiss: () -> Unit,
  onSave: (CourseEntity) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  val initialStatus =
    existingCourse?.let { CourseStatus.fromNameOrDefault(it.status) } ?: CourseStatus.GRADED
  val initialTab =
    when {
      initialStatus != CourseStatus.GRADED -> SheetInputTab.SPECIAL
      existingCourse != null &&
        MarksEntryMode.fromNameOrDefault(existingCourse.entryMode) ==
          MarksEntryMode.EXAM_BREAKDOWN -> SheetInputTab.BREAKDOWN
      else -> SheetInputTab.DIRECT
    }

  var courseName by remember { mutableStateOf(existingCourse?.name ?: "") }
  var creditText by remember {
    mutableStateOf(
      existingCourse?.let { formatCredits(it.creditHours) } ?: "3"
    )
  }
  var selectedTab by remember { mutableStateOf(initialTab) }
  var directMarksText by remember {
    mutableStateOf(
      if (existingCourse != null && existingCourse.directPercent > 0.0) {
        formatMarks(existingCourse.directPercent)
      } else {
        ""
      }
    )
  }
  var midMarksText by remember {
    mutableStateOf(existingCourse?.midMarks?.let { formatMarks(it) } ?: "")
  }
  var sessionalMarksText by remember {
    mutableStateOf(existingCourse?.sessionalMarks?.let { formatMarks(it) } ?: "")
  }
  var finalMarksText by remember {
    mutableStateOf(existingCourse?.finalMarks?.let { formatMarks(it) } ?: "")
  }
  var maxMarksText by remember {
    mutableStateOf(existingCourse?.maxMarks?.let { formatMarks(it) } ?: "100")
  }
  var selectedSpecialStatus by remember {
    mutableStateOf(
      if (initialStatus != CourseStatus.GRADED) initialStatus else CourseStatus.RESULT_LATE
    )
  }
  var validationError by remember { mutableStateOf<String?>(null) }

  val parsedCredits = creditText.toDoubleOrNull()
  val activeStatus =
    if (selectedTab == SheetInputTab.SPECIAL) selectedSpecialStatus else CourseStatus.GRADED

  val computedRawPercent: Double? =
    when (selectedTab) {
      SheetInputTab.DIRECT -> directMarksText.toDoubleOrNull()
      SheetInputTab.BREAKDOWN -> {
        val mid = midMarksText.toDoubleOrNull() ?: 0.0
        val sess = sessionalMarksText.toDoubleOrNull() ?: 0.0
        val fin = finalMarksText.toDoubleOrNull() ?: 0.0
        val maxM = maxMarksText.toDoubleOrNull() ?: 100.0
        if (midMarksText.isEmpty() && sessionalMarksText.isEmpty() && finalMarksText.isEmpty()) {
          null
        } else {
          UniversityGradingEngine.computeExamTotalPercentage(mid, sess, fin, maxM)
        }
      }
      SheetInputTab.SPECIAL -> null
    }

  val previewEvaluation =
    remember(courseName, parsedCredits, activeStatus, computedRawPercent) {
      UniversityGradingEngine.evaluateCourse(
        courseId = existingCourse?.id ?: 0L,
        courseName = courseName.ifBlank { "Course $nextCourseIndex" },
        creditHours = parsedCredits ?: 3.0,
        status = activeStatus,
        rawPercentage = computedRawPercent,
      )
    }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .navigationBarsPadding()
          .imePadding(),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (existingCourse == null) "Add Course" else "Edit Course",
          style = MaterialTheme.typography.headlineMedium,
        )
        IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_course_sheet_button")) {
          Icon(Icons.Rounded.Close, contentDescription = "Close editor")
        }
      }

      OutlinedTextField(
        value = courseName,
        onValueChange = { courseName = it },
        label = { Text("Course Title / Code (Optional)") },
        placeholder = { Text("e.g., CS-201 Data Structures") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        modifier = Modifier.fillMaxWidth().testTag("course_name_input"),
      )

      // Credit hours quick selector + custom input
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
          text = "Credit Hours",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          listOf("1", "2", "3", "4").forEach { preset ->
            FilterChip(
              selected = creditText == preset,
              onClick = {
                creditText = preset
                validationError = null
              },
              label = { Text("${preset} Cr") },
              modifier = Modifier.testTag("credit_chip_$preset"),
            )
          }
          OutlinedTextField(
            value = creditText,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                creditText = input
                validationError = null
              }
            },
            label = { Text("Custom") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("credit_hours_input"),
          )
        }
      }

      // Entry Mode selector
      SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SheetInputTab.entries.forEachIndexed { index, tab ->
          SegmentedButton(
            selected = selectedTab == tab,
            onClick = {
              selectedTab = tab
              validationError = null
            },
            shape = SegmentedButtonDefaults.itemShape(index = index, count = SheetInputTab.entries.size),
            modifier = Modifier.testTag("mode_tab_${tab.name.lowercase()}"),
          ) {
            Text(tab.title, maxLines = 1)
          }
        }
      }

      when (selectedTab) {
        SheetInputTab.DIRECT -> {
          OutlinedTextField(
            value = directMarksText,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                directMarksText = input
                validationError = null
              }
            },
            label = { Text("Obtained Marks % (0 – 100)") },
            placeholder = { Text("e.g., 69.4 or 82") },
            supportingText = {
              Text("Clause 14.3: Any fraction rounds up to the next integer mark (60.1 → 61)")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("direct_marks_input"),
          )
        }
        SheetInputTab.BREAKDOWN -> {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = midMarksText,
              onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                  midMarksText = input
                  validationError = null
                }
              },
              label = { Text("Mid (30)") },
              placeholder = { Text("19.0") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f).testTag("mid_marks_input"),
            )
            OutlinedTextField(
              value = sessionalMarksText,
              onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                  sessionalMarksText = input
                  validationError = null
                }
              },
              label = { Text("Sess (20)") },
              placeholder = { Text("10.3") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f).testTag("sessional_marks_input"),
            )
            OutlinedTextField(
              value = finalMarksText,
              onValueChange = { input ->
                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                  finalMarksText = input
                  validationError = null
                }
              },
              label = { Text("Final (50)") },
              placeholder = { Text("40.1") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f).testTag("final_marks_input"),
            )
          }
          OutlinedTextField(
            value = maxMarksText,
            onValueChange = { input ->
              if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d*$"))) {
                maxMarksText = input
                validationError = null
              }
            },
            label = { Text("Total Weight / Max Marks") },
            supportingText = { Text("Default is 100 (30 + 20 + 50 per Clause 14.3 example)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("max_marks_input"),
          )
        }
        SheetInputTab.SPECIAL -> {
          val specialStatuses =
            listOf(
              CourseStatus.RESULT_LATE,
              CourseStatus.WITHDRAWAL,
              CourseStatus.REPEAT,
              CourseStatus.INCOMPLETE,
              CourseStatus.EXEMPTED,
            )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            specialStatuses.forEach { st ->
              FilterChip(
                selected = selectedSpecialStatus == st,
                onClick = {
                  selectedSpecialStatus = st
                  validationError = null
                },
                leadingIcon =
                  if (selectedSpecialStatus == st) {
                    { Icon(Icons.Rounded.Check, contentDescription = null) }
                  } else null,
                label = { Text("${st.code} · ${st.displayName}") },
                modifier = Modifier.testTag("status_chip_${st.code.lowercase()}"),
              )
            }
          }
          Text(
            text = selectedSpecialStatus.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      // Live Evaluation Preview Card
      val badgeColors = rememberGradeVisualStyle(previewEvaluation.letterGrade)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "LIVE CLAUSE 14 PREVIEW",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (activeStatus == CourseStatus.GRADED) {
              val rawDisplay = computedRawPercent?.let { formatMarks(it) } ?: "0"
              val roundedDisplay = previewEvaluation.roundedMarks ?: 0
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "$rawDisplay% → $roundedDisplay%",
                  fontFamily = JetBrainsMonoFamily,
                  style = MaterialTheme.typography.titleMedium,
                )
                if (previewEvaluation.wasRoundedUp) {
                  Spacer(modifier = Modifier.width(6.dp))
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(
                        Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.height(12.dp),
                      )
                      Text(
                        text = "14.3",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                      )
                    }
                  }
                }
              }
            } else {
              Text(
                text = "${activeStatus.displayName} (${activeStatus.code})",
                style = MaterialTheme.typography.titleMedium,
              )
            }
            Text(
              text =
                if (previewEvaluation.countsInGpa) {
                  "GP ${formatGpa(previewEvaluation.gradePoint ?: 0.0)} × ${formatCredits(previewEvaluation.creditHours)} Cr = ${formatGpa(previewEvaluation.qualityPoints)} Pts"
                } else {
                  "Excluded from GPA/CGPA denominator"
                },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          Surface(shape = RoundedCornerShape(14.dp), color = badgeColors.container) {
            Column(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text(
                text = previewEvaluation.letterGrade,
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.titleLarge,
                color = badgeColors.content,
              )
              Text(
                text = previewEvaluation.gradePoint?.let { formatGpa(it) } ?: "----",
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColors.content,
              )
            }
          }
        }
      }

      if (validationError != null) {
        Text(
          text = validationError!!,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
      }

      Button(
        onClick = {
          val credits = creditText.toDoubleOrNull()
          if (credits == null || credits <= 0.0 || credits > 20.0) {
            validationError = "Please enter valid credit hours (between 0.5 and 20)."
            return@Button
          }
          if (selectedTab == SheetInputTab.DIRECT) {
            val marks = directMarksText.toDoubleOrNull()
            if (marks == null || marks < 0.0 || marks > 100.0) {
              validationError = "Please enter marks percentage between 0 and 100."
              return@Button
            }
          } else if (selectedTab == SheetInputTab.BREAKDOWN) {
            val maxM = maxMarksText.toDoubleOrNull()
            if (maxM == null || maxM <= 0.0) {
              validationError = "Max marks must be greater than 0."
              return@Button
            }
            val mid = midMarksText.toDoubleOrNull() ?: 0.0
            val sess = sessionalMarksText.toDoubleOrNull() ?: 0.0
            val fin = finalMarksText.toDoubleOrNull() ?: 0.0
            if (midMarksText.isEmpty() && sessionalMarksText.isEmpty() && finalMarksText.isEmpty()) {
              validationError = "Enter at least one exam component mark."
              return@Button
            }
            if (mid + sess + fin > maxM) {
              validationError = "Sum of marks (${formatMarks(mid + sess + fin)}) exceeds max marks (${formatMarks(maxM)})."
              return@Button
            }
          }

          val finalName = courseName.trim().ifEmpty { "Course $nextCourseIndex" }
          val entity =
            CourseEntity(
              id = existingCourse?.id ?: 0L,
              semesterId = semesterId,
              name = finalName,
              creditHours = credits,
              status = activeStatus.name,
              entryMode =
                if (selectedTab == SheetInputTab.BREAKDOWN) MarksEntryMode.EXAM_BREAKDOWN.name
                else MarksEntryMode.DIRECT_PERCENT.name,
              directPercent = directMarksText.toDoubleOrNull()?.coerceIn(0.0, 100.0) ?: 0.0,
              midMarks =
                if (selectedTab == SheetInputTab.BREAKDOWN) midMarksText.toDoubleOrNull() ?: 0.0
                else null,
              sessionalMarks =
                if (selectedTab == SheetInputTab.BREAKDOWN) sessionalMarksText.toDoubleOrNull() ?: 0.0
                else null,
              finalMarks =
                if (selectedTab == SheetInputTab.BREAKDOWN) finalMarksText.toDoubleOrNull() ?: 0.0
                else null,
              maxMarks = maxMarksText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 100.0,
              sortOrder = existingCourse?.sortOrder ?: 0,
            )
          onSave(entity)
        },
        modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_course_button"),
        shape = RoundedCornerShape(14.dp),
      ) {
        Text(if (existingCourse == null) "Save Course" else "Update Course")
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
