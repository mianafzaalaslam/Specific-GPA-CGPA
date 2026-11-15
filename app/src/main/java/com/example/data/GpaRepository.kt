package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GpaRepository(private val dao: GpaDao) {

  val semestersWithCourses: Flow<List<SemesterWithCourses>> = dao.observeSemestersWithCourses()

  val priorAcademic: Flow<PriorAcademicEntity> =
    dao.observePriorAcademic().map { it ?: PriorAcademicEntity() }

  suspend fun ensureInitialSemester(): Long {
    val count = dao.getSemesterCount()
    if (count == 0) {
      return dao.insertSemester(SemesterEntity(title = "Semester 1", sortOrder = 0))
    }
    return -1L
  }

  suspend fun addSemester(
    title: String,
    sortOrder: Int,
    isSummaryOnly: Boolean = false,
    summaryGpa: Double = 0.0,
    summaryCredits: Double = 0.0,
  ): Long {
    return dao.insertSemester(
      SemesterEntity(
        title = title.trim().ifEmpty { "Semester ${sortOrder + 1}" },
        sortOrder = sortOrder,
        isSummaryOnly = isSummaryOnly,
        summaryGpa = summaryGpa,
        summaryCredits = summaryCredits,
      )
    )
  }

  suspend fun updateSemester(semester: SemesterEntity) {
    dao.updateSemester(semester)
  }

  suspend fun deleteSemester(semester: SemesterEntity) {
    dao.deleteSemester(semester)
  }

  suspend fun saveCourse(course: CourseEntity): Long {
    return if (course.id == 0L) {
      dao.insertCourse(course)
    } else {
      dao.updateCourse(course)
      course.id
    }
  }

  suspend fun deleteCourse(courseId: Long) {
    dao.deleteCourseById(courseId)
  }

  suspend fun savePriorAcademic(enabled: Boolean, priorCgpa: Double, priorCreditHours: Double) {
    dao.upsertPriorAcademic(
      PriorAcademicEntity(
        id = 1,
        enabled = enabled,
        priorCgpa = priorCgpa,
        priorCreditHours = priorCreditHours,
      )
    )
  }
}
