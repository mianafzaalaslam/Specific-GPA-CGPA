package com.example

import com.example.domain.CourseStatus
import com.example.domain.UniversityGradingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversityGradingEngineTest {

  @Test
  fun `clause 14_3 rounds any positive fraction up to next integer mark`() {
    assertEquals(61, UniversityGradingEngine.roundMarksClause14_3(60.1))
    assertEquals(61, UniversityGradingEngine.roundMarksClause14_3(60.9))
    assertEquals(60, UniversityGradingEngine.roundMarksClause14_3(60.0))

    val rawExample =
      UniversityGradingEngine.computeExamTotalPercentage(
        midTerm = 19.0,
        sessional = 10.3,
        finalTerm = 40.1,
        maxMarks = 100.0,
      )
    assertEquals(69.4, rawExample, 0.0001)
    assertEquals(70, UniversityGradingEngine.roundMarksClause14_3(rawExample))

    val eval =
      UniversityGradingEngine.evaluateCourse(
        courseId = 1L,
        courseName = "Official Example",
        creditHours = 3.0,
        status = CourseStatus.GRADED,
        rawPercentage = rawExample,
      )
    assertEquals(70, eval.roundedMarks)
    assertTrue(eval.wasRoundedUp)
    assertEquals("B", eval.letterGrade)
    assertEquals(3.14, eval.gradePoint!!, 0.0001)
  }

  @Test
  fun `clause 14_2 grading table matches all key boundary marks`() {
    val expected =
      mapOf(
        100 to Pair(4.00, "A+"),
        85 to Pair(4.00, "A+"),
        84 to Pair(3.94, "A"),
        83 to Pair(3.88, "A"),
        82 to Pair(3.82, "A"),
        81 to Pair(3.77, "A"),
        80 to Pair(3.71, "A"),
        79 to Pair(3.65, "B"),
        75 to Pair(3.43, "B"),
        70 to Pair(3.14, "B"),
        65 to Pair(2.86, "B"),
        64 to Pair(2.80, "C"),
        60 to Pair(2.57, "C"),
        55 to Pair(2.29, "C"),
        54 to Pair(2.23, "D"),
        50 to Pair(2.00, "D"),
        49 to Pair(0.00, "F"),
        0 to Pair(0.00, "F"),
      )

    for ((mark, pair) in expected) {
      val entry = UniversityGradingEngine.lookupNumericGrade(mark)
      assertEquals("GP mismatch for $mark", pair.first, entry.gradePoint!!, 0.0001)
      assertEquals("Letter mismatch for $mark", pair.second, entry.letterGrade)
    }
  }

  @Test
  fun `special statuses handle exclusion and zero gp properly`() {
    val rl = UniversityGradingEngine.evaluateCourse(1L, "Course RL", 3.0, CourseStatus.RESULT_LATE, null)
    val w = UniversityGradingEngine.evaluateCourse(2L, "Course W", 3.0, CourseStatus.WITHDRAWAL, null)
    val r = UniversityGradingEngine.evaluateCourse(3L, "Course R", 3.0, CourseStatus.REPEAT, null)
    val ex = UniversityGradingEngine.evaluateCourse(4L, "Course EX", 3.0, CourseStatus.EXEMPTED, null)
    val inc = UniversityGradingEngine.evaluateCourse(5L, "Course I", 3.0, CourseStatus.INCOMPLETE, null)

    assertFalse(rl.countsInGpa)
    assertNull(rl.gradePoint)
    assertFalse(w.countsInGpa)
    assertFalse(r.countsInGpa)
    assertFalse(ex.countsInGpa)
    assertTrue(ex.isPassing)

    assertTrue(inc.countsInGpa)
    assertEquals(0.00, inc.gradePoint!!, 0.0001)
    assertFalse(inc.isPassing)
  }

  @Test
  fun `clause 14_4 weighted semester gpa and cgpa with prior baseline`() {
    val c1 = UniversityGradingEngine.evaluateCourse(1L, "Math", 3.0, CourseStatus.GRADED, 85.0) // 4.00 * 3 = 12.00
    val c2 = UniversityGradingEngine.evaluateCourse(2L, "Physics", 4.0, CourseStatus.GRADED, 69.4) // 3.14 * 4 = 12.56
    val c3 = UniversityGradingEngine.evaluateCourse(3L, "Lab", 1.0, CourseStatus.WITHDRAWAL, null) // excluded

    val sem1 =
      UniversityGradingEngine.calculateSemesterSummary(
        semesterId = 1L,
        title = "Semester 1",
        isSummaryOnly = false,
        includedInCgpa = true,
        summaryGpa = 0.0,
        summaryCredits = 0.0,
        evaluations = listOf(c1, c2, c3),
      )

    assertEquals(7.0, sem1.gpaCreditHours, 0.0001)
    assertEquals(24.56, sem1.totalQualityPoints, 0.0001)
    assertEquals(24.56 / 7.0, sem1.gpa, 0.0001)

    // Combine with prior 3.00 CGPA over 10 credit hours -> (30.00 + 24.56) / 17.0 = 54.56 / 17.0
    val cgpa =
      UniversityGradingEngine.calculateCgpa(
        semesters = listOf(sem1),
        priorEnabled = true,
        priorCgpa = 3.00,
        priorCredits = 10.0,
      )
    assertEquals(17.0, cgpa.totalCreditHours, 0.0001)
    assertEquals(54.56 / 17.0, cgpa.cgpa, 0.0001)
  }
}
