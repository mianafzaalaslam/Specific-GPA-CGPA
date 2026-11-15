package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.PriorAcademicEntity
import com.example.data.SemesterEntity

@Composable
fun AddSemesterDialog(
  defaultTitle: String,
  existingSemester: SemesterEntity? = null,
  onDismiss: () -> Unit,
  onConfirmNew: (title: String, isSummaryOnly: Boolean, summaryGpa: Double, summaryCredits: Double) -> Unit,
  onConfirmUpdate: (semester: SemesterEntity, title: String, summaryGpa: Double, summaryCredits: Double) -> Unit =
    { _, _, _, _ -> },
) {
  var title by remember { mutableStateOf(existingSemester?.title ?: defaultTitle) }
  var isSummaryOnly by remember { mutableStateOf(existingSemester?.isSummaryOnly ?: false) }
  var gpaText by remember {
    mutableStateOf(
      if (existingSemester != null && existingSemester.isSummaryOnly) {
        formatGpa(existingSemester.summaryGpa)
      } else {
        ""
      }
    )
  }
  var creditsText by remember {
    mutableStateOf(
      if (existingSemester != null && existingSemester.isSummaryOnly) {
        formatCredits(existingSemester.summaryCredits)
      } else {
        ""
      }
    )
  }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (existingSemester == null) "Add Semester" else "Edit Semester")
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = {
            title = it
            errorText = null
          },
          label = { Text("Semester Name") },
          placeholder = { Text("e.g., Fall 2026 or Semester 2") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
          modifier = Modifier.fillMaxWidth().testTag("semester_title_input"),
        )

        if (existingSemester == null) {
          SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
              selected = !isSummaryOnly,
              onClick = {
                isSummaryOnly = false
                errorText = null
              },
              shape = SegmentedButtonDefaults.itemShape(0, 2),
              modifier = Modifier.testTag("semester_mode_courses"),
            ) {
              Text("With Courses")
            }
            SegmentedButton(
              selected = isSummaryOnly,
              onClick = {
                isSummaryOnly = true
                errorText = null
              },
              shape = SegmentedButtonDefaults.itemShape(1, 2),
              modifier = Modifier.testTag("semester_mode_summary"),
            ) {
              Text("GPA Summary")
            }
          }
        }

        if (isSummaryOnly) {
          Text(
            text = "Quickly include a past semester using its final SGPA and total credit hours.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = gpaText,
              onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                  gpaText = it
                  errorText = null
                }
              },
              label = { Text("SGPA (0–4)") },
              placeholder = { Text("3.45") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f).testTag("summary_gpa_input"),
            )
            OutlinedTextField(
              value = creditsText,
              onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                  creditsText = it
                  errorText = null
                }
              },
              label = { Text("Credits") },
              placeholder = { Text("18") },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              modifier = Modifier.weight(1f).testTag("summary_credits_input"),
            )
          }
        }

        if (errorText != null) {
          Text(
            text = errorText!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          val finalTitle = title.trim().ifEmpty { defaultTitle }
          if (isSummaryOnly) {
            val g = gpaText.toDoubleOrNull()
            val c = creditsText.toDoubleOrNull()
            if (g == null || g < 0.0 || g > 4.0) {
              errorText = "Enter a valid SGPA between 0.00 and 4.00."
              return@TextButton
            }
            if (c == null || c <= 0.0) {
              errorText = "Enter valid credit hours (> 0)."
              return@TextButton
            }
            if (existingSemester == null) {
              onConfirmNew(finalTitle, true, g, c)
            } else {
              onConfirmUpdate(existingSemester, finalTitle, g, c)
            }
          } else {
            if (existingSemester == null) {
              onConfirmNew(finalTitle, false, 0.0, 0.0)
            } else {
              onConfirmUpdate(existingSemester, finalTitle, 0.0, 0.0)
            }
          }
        },
        modifier = Modifier.testTag("confirm_semester_button"),
      ) {
        Text(if (existingSemester == null) "Create" else "Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
fun PriorAcademicDialog(
  prior: PriorAcademicEntity,
  onDismiss: () -> Unit,
  onSave: (enabled: Boolean, cgpa: Double, credits: Double) -> Unit,
) {
  var enabled by remember { mutableStateOf(prior.enabled || prior.priorCreditHours == 0.0) }
  var cgpaText by remember {
    mutableStateOf(if (prior.priorCreditHours > 0.0) formatGpa(prior.priorCgpa) else "")
  }
  var creditsText by remember {
    mutableStateOf(if (prior.priorCreditHours > 0.0) formatCredits(prior.priorCreditHours) else "")
  }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Prior CGPA Baseline") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text =
            "Already completed earlier semesters? Enter your existing CGPA and total graded credit hours to combine them accurately via Clause 14.4 weighting.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Include Prior Record", style = MaterialTheme.typography.titleSmall)
          Switch(
            checked = enabled,
            onCheckedChange = {
              enabled = it
              errorText = null
            },
            modifier = Modifier.testTag("prior_enabled_switch"),
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = cgpaText,
            onValueChange = {
              if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                cgpaText = it
                errorText = null
              }
            },
            enabled = enabled,
            label = { Text("Prior CGPA") },
            placeholder = { Text("3.25") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("prior_cgpa_input"),
          )
          OutlinedTextField(
            value = creditsText,
            onValueChange = {
              if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                creditsText = it
                errorText = null
              }
            },
            enabled = enabled,
            label = { Text("Prior Credits") },
            placeholder = { Text("64") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("prior_credits_input"),
          )
        }
        if (errorText != null) {
          Text(
            text = errorText!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
          )
        }
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (!enabled) {
            onSave(false, cgpaText.toDoubleOrNull() ?: 0.0, creditsText.toDoubleOrNull() ?: 0.0)
            return@TextButton
          }
          val g = cgpaText.toDoubleOrNull()
          val c = creditsText.toDoubleOrNull()
          if (g == null || g < 0.0 || g > 4.0) {
            errorText = "Enter a valid prior CGPA between 0.00 and 4.00."
            return@TextButton
          }
          if (c == null || c <= 0.0) {
            errorText = "Enter prior credit hours greater than 0."
            return@TextButton
          }
          onSave(true, g, c)
        },
        modifier = Modifier.testTag("save_prior_button"),
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}
