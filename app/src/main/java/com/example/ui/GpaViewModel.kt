package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CourseEntity
import com.example.data.GpaRepository
import com.example.data.PriorAcademicEntity
import com.example.data.SemesterEntity
import com.example.data.SemesterWithCourses
import com.example.domain.CgpaSummary
import com.example.domain.CourseStatus
import com.example.domain.MarksEntryMode
import com.example.domain.SemesterSummary
import com.example.domain.UniversityGradingEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
  GPA("GPA"),
  CGPA("CGPA"),
  SCHEME("Scheme"),
}

data class GpaUiState(
  val isLoading: Boolean = true,
  val semestersWithCourses: List<SemesterWithCourses> = emptyList(),
  val semesterSummaries: List<SemesterSummary> = emptyList(),
  val activeSemesterId: Long? = null,
  val activeSemesterEntity: SemesterEntity? = null,
  val activeSemesterSummary: SemesterSummary? = null,
  val activeCourses: List<CourseEntity> = emptyList(),
  val priorAcademic: PriorAcademicEntity = PriorAcademicEntity(),
  val cgpaSummary: CgpaSummary =
    CgpaSummary(
      cgpa = 0.0,
      totalQualityPoints = 0.0,
      totalCreditHours = 0.0,
      totalEarnedCreditHours = 0.0,
      includedSemesterCount = 0,
      hasPriorBaseline = false,
    ),
)

class GpaViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: GpaRepository =
    GpaRepository(AppDatabase.getInstance(application).gpaDao())

  private val selectedSemesterIdFlow = MutableStateFlow<Long?>(null)

  init {
    viewModelScope.launch {
      val createdId = repository.ensureInitialSemester()
      if (createdId > 0L && selectedSemesterIdFlow.value == null) {
        selectedSemesterIdFlow.value = createdId
      }
    }
  }

  val uiState: StateFlow<GpaUiState> =
    combine(
        repository.semestersWithCourses,
        repository.priorAcademic,
        selectedSemesterIdFlow,
      ) { semestersWithCourses, prior, selectedId ->
        val summaries =
          semestersWithCourses.map { item ->
            val sortedCourses = item.courses.sortedWith(compareBy({ it.sortOrder }, { it.id }))
            val evaluations =
              sortedCourses.map { course ->
                val status = CourseStatus.fromNameOrDefault(course.status)
                val mode = MarksEntryMode.fromNameOrDefault(course.entryMode)
                val rawPercentage =
                  if (status == CourseStatus.GRADED) {
                    if (mode == MarksEntryMode.EXAM_BREAKDOWN) {
                      UniversityGradingEngine.computeExamTotalPercentage(
                        midTerm = course.midMarks ?: 0.0,
                        sessional = course.sessionalMarks ?: 0.0,
                        finalTerm = course.finalMarks ?: 0.0,
                        maxMarks = course.maxMarks,
                      )
                    } else {
                      course.directPercent
                    }
                  } else {
                    null
                  }
                UniversityGradingEngine.evaluateCourse(
                  courseId = course.id,
                  courseName = course.name,
                  creditHours = course.creditHours,
                  status = status,
                  rawPercentage = rawPercentage,
                )
              }
            UniversityGradingEngine.calculateSemesterSummary(
              semesterId = item.semester.id,
              title = item.semester.title,
              isSummaryOnly = item.semester.isSummaryOnly,
              includedInCgpa = item.semester.includedInCgpa,
              summaryGpa = item.semester.summaryGpa,
              summaryCredits = item.semester.summaryCredits,
              evaluations = evaluations,
            )
          }

        val resolvedActiveId =
          when {
            selectedId != null && semestersWithCourses.any { it.semester.id == selectedId } ->
              selectedId
            else -> semestersWithCourses.firstOrNull { !it.semester.isSummaryOnly }?.semester?.id
              ?: semestersWithCourses.firstOrNull()?.semester?.id
          }

        val activePair = semestersWithCourses.firstOrNull { it.semester.id == resolvedActiveId }
        val activeSummary = summaries.firstOrNull { it.semesterId == resolvedActiveId }
        val cgpaSummary =
          UniversityGradingEngine.calculateCgpa(
            semesters = summaries,
            priorEnabled = prior.enabled,
            priorCgpa = prior.priorCgpa,
            priorCredits = prior.priorCreditHours,
          )

        GpaUiState(
          isLoading = false,
          semestersWithCourses = semestersWithCourses,
          semesterSummaries = summaries,
          activeSemesterId = resolvedActiveId,
          activeSemesterEntity = activePair?.semester,
          activeSemesterSummary = activeSummary,
          activeCourses = activePair?.courses?.sortedWith(compareBy({ it.sortOrder }, { it.id }))
            ?: emptyList(),
          priorAcademic = prior,
          cgpaSummary = cgpaSummary,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GpaUiState(),
      )

  fun selectSemester(semesterId: Long) {
    selectedSemesterIdFlow.value = semesterId
  }

  fun addSemester(
    title: String,
    isSummaryOnly: Boolean = false,
    summaryGpa: Double = 0.0,
    summaryCredits: Double = 0.0,
  ) {
    viewModelScope.launch {
      val nextOrder = (uiState.value.semestersWithCourses.maxOfOrNull { it.semester.sortOrder } ?: -1) + 1
      val newId =
        repository.addSemester(
          title = title,
          sortOrder = nextOrder,
          isSummaryOnly = isSummaryOnly,
          summaryGpa = summaryGpa,
          summaryCredits = summaryCredits,
        )
      if (!isSummaryOnly) {
        selectedSemesterIdFlow.value = newId
      }
    }
  }

  fun renameSemester(semester: SemesterEntity, newTitle: String) {
    val trimmed = newTitle.trim()
    if (trimmed.isEmpty()) return
    viewModelScope.launch { repository.updateSemester(semester.copy(title = trimmed)) }
  }

  fun toggleSemesterIncluded(semester: SemesterEntity) {
    viewModelScope.launch {
      repository.updateSemester(semester.copy(includedInCgpa = !semester.includedInCgpa))
    }
  }

  fun updateSummarySemester(semester: SemesterEntity, title: String, gpa: Double, credits: Double) {
    viewModelScope.launch {
      repository.updateSemester(
        semester.copy(
          title = title.trim().ifEmpty { semester.title },
          summaryGpa = gpa.coerceIn(0.0, 4.0),
          summaryCredits = credits.coerceAtLeast(0.0),
        )
      )
    }
  }

  fun deleteSemester(semester: SemesterEntity) {
    viewModelScope.launch {
      repository.deleteSemester(semester)
      val remaining = uiState.value.semestersWithCourses.filter { it.semester.id != semester.id }
      if (remaining.isEmpty()) {
        val created = repository.addSemester("Semester 1", sortOrder = 0)
        selectedSemesterIdFlow.value = created
      } else if (selectedSemesterIdFlow.value == semester.id) {
        selectedSemesterIdFlow.value = remaining.first().semester.id
      }
    }
  }

  fun saveCourse(course: CourseEntity) {
    viewModelScope.launch {
      val nextOrder =
        if (course.id == 0L) {
          (uiState.value.activeCourses.maxOfOrNull { it.sortOrder } ?: -1) + 1
        } else {
          course.sortOrder
        }
      repository.saveCourse(course.copy(sortOrder = nextOrder))
    }
  }

  fun deleteCourse(courseId: Long) {
    viewModelScope.launch { repository.deleteCourse(courseId) }
  }

  fun savePriorAcademic(enabled: Boolean, priorCgpa: Double, priorCredits: Double) {
    viewModelScope.launch {
      repository.savePriorAcademic(
        enabled = enabled,
        priorCgpa = priorCgpa.coerceIn(0.0, 4.0),
        priorCreditHours = priorCredits.coerceAtLeast(0.0),
      )
    }
  }
}
