package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min

enum class CourseStatus(
  val code: String,
  val displayName: String,
  val countsInGpa: Boolean,
  val fixedGradePoint: Double?,
  val description: String,
) {
  GRADED(
    code = "MARKS",
    displayName = "Numeric Marks",
    countsInGpa = true,
    fixedGradePoint = null,
    description = "Evaluated via Clause 14.2 & 14.3",
  ),
  RESULT_LATE(
    code = "RL",
    displayName = "Result Late",
    countsInGpa = false,
    fixedGradePoint = null,
    description = "Excluded until marks are declared",
  ),
  WITHDRAWAL(
    code = "W",
    displayName = "Withdrawal",
    countsInGpa = false,
    fixedGradePoint = null,
    description = "Excluded from GPA/CGPA",
  ),
  REPEAT(
    code = "R",
    displayName = "Repeat",
    countsInGpa = false,
    fixedGradePoint = null,
    description = "Excluded pending repeat grade",
  ),
  INCOMPLETE(
    code = "I",
    displayName = "Incomplete",
    countsInGpa = true,
    fixedGradePoint = 0.00,
    description = "0.00 Grade Point in GPA calculation",
  ),
  EXEMPTED(
    code = "EX",
    displayName = "Exempted",
    countsInGpa = false,
    fixedGradePoint = null,
    description = "Excluded from GPA/CGPA",
  );

  companion object {
    fun fromNameOrDefault(value: String): CourseStatus =
      entries.firstOrNull { it.name == value } ?: GRADED
  }
}

enum class MarksEntryMode {
  DIRECT_PERCENT,
  EXAM_BREAKDOWN;

  companion object {
    fun fromNameOrDefault(value: String): MarksEntryMode =
      entries.firstOrNull { it.name == value } ?: DIRECT_PERCENT
  }
}

data class GradeScaleEntry(
  val marksLabel: String,
  val minMark: Int?,
  val maxMark: Int?,
  val gradePoint: Double?,
  val gradePointDisplay: String,
  val letterGrade: String,
  val category: GradeCategory,
)

enum class GradeCategory {
  A_GROUP,
  B_GROUP,
  C_GROUP,
  D_GROUP,
  FAIL,
  SPECIAL,
}

data class CourseEvaluation(
  val courseId: Long,
  val courseName: String,
  val creditHours: Double,
  val status: CourseStatus,
  val rawMarks: Double?,
  val roundedMarks: Int?,
  val wasRoundedUp: Boolean,
  val gradePoint: Double?,
  val letterGrade: String,
  val qualityPoints: Double,
  val countsInGpa: Boolean,
  val isPassing: Boolean,
)

data class SemesterSummary(
  val semesterId: Long,
  val title: String,
  val isSummaryOnly: Boolean,
  val includedInCgpa: Boolean,
  val gpa: Double,
  val totalQualityPoints: Double,
  val gpaCreditHours: Double,
  val earnedCreditHours: Double,
  val courseCount: Int,
  val gradedCourseCount: Int,
  val evaluations: List<CourseEvaluation>,
)

data class CgpaSummary(
  val cgpa: Double,
  val totalQualityPoints: Double,
  val totalCreditHours: Double,
  val totalEarnedCreditHours: Double,
  val includedSemesterCount: Int,
  val hasPriorBaseline: Boolean,
)

data class TargetPlanResult(
  val isReachable: Boolean,
  val requiredNextGpa: Double,
  val projectedMaxCgpa: Double,
  val projectedMinCgpa: Double,
)

object UniversityGradingEngine {

  const val PASSING_MARK_PERCENT = 50
  const val MAX_GPA = 4.00

  /** Exact Clause 14.2 Grading Table from University Regulations. */
  val officialGradingTable: List<GradeScaleEntry> =
    listOf(
      GradeScaleEntry("85-100", 85, 100, 4.00, "4.00", "A+", GradeCategory.A_GROUP),
      GradeScaleEntry("84", 84, 84, 3.94, "3.94", "A", GradeCategory.A_GROUP),
      GradeScaleEntry("83", 83, 83, 3.88, "3.88", "A", GradeCategory.A_GROUP),
      GradeScaleEntry("82", 82, 82, 3.82, "3.82", "A", GradeCategory.A_GROUP),
      GradeScaleEntry("81", 81, 81, 3.77, "3.77", "A", GradeCategory.A_GROUP),
      GradeScaleEntry("80", 80, 80, 3.71, "3.71", "A", GradeCategory.A_GROUP),
      GradeScaleEntry("79", 79, 79, 3.65, "3.65", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("78", 78, 78, 3.60, "3.60", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("77", 77, 77, 3.54, "3.54", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("76", 76, 76, 3.48, "3.48", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("75", 75, 75, 3.43, "3.43", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("74", 74, 74, 3.37, "3.37", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("73", 73, 73, 3.31, "3.31", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("72", 72, 72, 3.25, "3.25", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("71", 71, 71, 3.20, "3.20", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("70", 70, 70, 3.14, "3.14", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("69", 69, 69, 3.08, "3.08", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("68", 68, 68, 3.03, "3.03", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("67", 67, 67, 2.97, "2.97", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("66", 66, 66, 2.91, "2.91", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("65", 65, 65, 2.86, "2.86", "B", GradeCategory.B_GROUP),
      GradeScaleEntry("64", 64, 64, 2.80, "2.80", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("63", 63, 63, 2.74, "2.74", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("62", 62, 62, 2.68, "2.68", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("61", 61, 61, 2.63, "2.63", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("60", 60, 60, 2.57, "2.57", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("59", 59, 59, 2.51, "2.51", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("58", 58, 58, 2.46, "2.46", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("57", 57, 57, 2.40, "2.40", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("56", 56, 56, 2.34, "2.34", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("55", 55, 55, 2.29, "2.29", "C", GradeCategory.C_GROUP),
      GradeScaleEntry("54", 54, 54, 2.23, "2.23", "D", GradeCategory.D_GROUP),
      GradeScaleEntry("53", 53, 53, 2.17, "2.17", "D", GradeCategory.D_GROUP),
      GradeScaleEntry("52", 52, 52, 2.11, "2.11", "D", GradeCategory.D_GROUP),
      GradeScaleEntry("51", 51, 51, 2.06, "2.06", "D", GradeCategory.D_GROUP),
      GradeScaleEntry("50", 50, 50, 2.00, "2.00", "D", GradeCategory.D_GROUP),
      GradeScaleEntry("Below 50 Marks", 0, 49, 0.00, "Fail", "F", GradeCategory.FAIL),
      GradeScaleEntry("Result Late", null, null, null, "----", "RL", GradeCategory.SPECIAL),
      GradeScaleEntry("Withdrawal", null, null, null, "----", "W", GradeCategory.SPECIAL),
      GradeScaleEntry("Repeat", null, null, null, "----", "R", GradeCategory.SPECIAL),
      GradeScaleEntry("Incomplete", null, null, 0.00, "0.00", "I", GradeCategory.SPECIAL),
      GradeScaleEntry("Exempted", null, null, null, "----", "EX", GradeCategory.SPECIAL),
    )

  private val numericGradeMap: Map<Int, GradeScaleEntry> = buildMap {
    for (entry in officialGradingTable) {
      val minM = entry.minMark ?: continue
      val maxM = entry.maxMark ?: continue
      for (m in minM..maxM) {
        put(m, entry)
      }
    }
  }

  /**
   * Clause 14.3: Fraction of marks obtained in a course shall be counted as one mark,
   * e.g. 60.1 and 60.9 shall be considered as 61, and 69.4 shall be rounded to 70.
   * Uses BigDecimal scaled to 4 decimal places first to eliminate IEEE-754 float noise.
   */
  fun roundMarksClause14_3(rawMarks: Double): Int {
    if (rawMarks.isNaN() || rawMarks.isInfinite()) return 0
    val clamped = min(100.0, max(0.0, rawMarks))
    val cleanDecimal = BigDecimal.valueOf(clamped).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros()
    return cleanDecimal.setScale(0, RoundingMode.CEILING).toInt().coerceIn(0, 100)
  }

  /**
   * Computes exact percentage from Mid Term, Sessional, and Final Term marks using BigDecimal
   * so e.g. 19.0 + 10.3 + 40.1 = 69.4 exactly.
   */
  fun computeExamTotalPercentage(
    midTerm: Double,
    sessional: Double,
    finalTerm: Double,
    maxMarks: Double = 100.0,
  ): Double {
    val safeMax = if (maxMarks <= 0.0) 100.0 else maxMarks
    val sum =
      BigDecimal.valueOf(max(0.0, midTerm))
        .add(BigDecimal.valueOf(max(0.0, sessional)))
        .add(BigDecimal.valueOf(max(0.0, finalTerm)))
    val percent =
      if (safeMax == 100.0) {
        sum
      } else {
        sum.multiply(BigDecimal.valueOf(100.0))
          .divide(BigDecimal.valueOf(safeMax), 4, RoundingMode.HALF_UP)
      }
    return percent.toDouble().coerceIn(0.0, 100.0)
  }

  fun lookupNumericGrade(roundedMarks: Int): GradeScaleEntry {
    val clamped = roundedMarks.coerceIn(0, 100)
    return numericGradeMap[clamped] ?: officialGradingTable.first { it.letterGrade == "F" }
  }

  fun evaluateCourse(
    courseId: Long,
    courseName: String,
    creditHours: Double,
    status: CourseStatus,
    rawPercentage: Double?,
  ): CourseEvaluation {
    val safeCredits = max(0.0, creditHours)
    if (status != CourseStatus.GRADED) {
      val gp = status.fixedGradePoint
      val qp = if (status.countsInGpa && gp != null) gp * safeCredits else 0.0
      return CourseEvaluation(
        courseId = courseId,
        courseName = courseName,
        creditHours = safeCredits,
        status = status,
        rawMarks = null,
        roundedMarks = null,
        wasRoundedUp = false,
        gradePoint = gp,
        letterGrade = status.code,
        qualityPoints = qp,
        countsInGpa = status.countsInGpa && safeCredits > 0.0,
        isPassing = status == CourseStatus.EXEMPTED,
      )
    }

    val safeRaw = (rawPercentage ?: 0.0).coerceIn(0.0, 100.0)
    val cleanRaw =
      BigDecimal.valueOf(safeRaw).setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toDouble()
    val rounded = roundMarksClause14_3(cleanRaw)
    val wasRoundedUp = rounded.toDouble() > cleanRaw
    val scaleEntry = lookupNumericGrade(rounded)
    val gp = scaleEntry.gradePoint ?: 0.0
    val qp = gp * safeCredits

    return CourseEvaluation(
      courseId = courseId,
      courseName = courseName,
      creditHours = safeCredits,
      status = status,
      rawMarks = cleanRaw,
      roundedMarks = rounded,
      wasRoundedUp = wasRoundedUp,
      gradePoint = gp,
      letterGrade = scaleEntry.letterGrade,
      qualityPoints = qp,
      countsInGpa = safeCredits > 0.0,
      isPassing = rounded >= PASSING_MARK_PERCENT,
    )
  }

  fun calculateSemesterSummary(
    semesterId: Long,
    title: String,
    isSummaryOnly: Boolean,
    includedInCgpa: Boolean,
    summaryGpa: Double,
    summaryCredits: Double,
    evaluations: List<CourseEvaluation>,
  ): SemesterSummary {
    if (isSummaryOnly) {
      val safeCredits = max(0.0, summaryCredits)
      val safeGpa = summaryGpa.coerceIn(0.0, MAX_GPA)
      return SemesterSummary(
        semesterId = semesterId,
        title = title,
        isSummaryOnly = true,
        includedInCgpa = includedInCgpa,
        gpa = safeGpa,
        totalQualityPoints = safeGpa * safeCredits,
        gpaCreditHours = safeCredits,
        earnedCreditHours = if (safeGpa >= 2.0) safeCredits else 0.0,
        courseCount = 0,
        gradedCourseCount = 0,
        evaluations = emptyList(),
      )
    }

    val counted = evaluations.filter { it.countsInGpa }
    val totalCredits = counted.sumOf { it.creditHours }
    val totalQualityPoints = counted.sumOf { it.qualityPoints }
    val gpa = if (totalCredits > 0.0) totalQualityPoints / totalCredits else 0.0
    val earnedCredits = evaluations.filter { it.isPassing }.sumOf { it.creditHours }

    return SemesterSummary(
      semesterId = semesterId,
      title = title,
      isSummaryOnly = false,
      includedInCgpa = includedInCgpa,
      gpa = gpa.coerceIn(0.0, MAX_GPA),
      totalQualityPoints = totalQualityPoints,
      gpaCreditHours = totalCredits,
      earnedCreditHours = earnedCredits,
      courseCount = evaluations.size,
      gradedCourseCount = counted.size,
      evaluations = evaluations,
    )
  }

  fun calculateCgpa(
    semesters: List<SemesterSummary>,
    priorEnabled: Boolean,
    priorCgpa: Double,
    priorCredits: Double,
  ): CgpaSummary {
    val includedSemesters = semesters.filter { it.includedInCgpa }
    var totalQp = includedSemesters.sumOf { it.totalQualityPoints }
    var totalCredits = includedSemesters.sumOf { it.gpaCreditHours }
    var totalEarned = includedSemesters.sumOf { it.earnedCreditHours }

    val usePrior = priorEnabled && priorCredits > 0.0
    if (usePrior) {
      val safePriorCredits = max(0.0, priorCredits)
      val safePriorCgpa = priorCgpa.coerceIn(0.0, MAX_GPA)
      totalQp += safePriorCgpa * safePriorCredits
      totalCredits += safePriorCredits
      totalEarned += safePriorCredits
    }

    val cgpa = if (totalCredits > 0.0) (totalQp / totalCredits).coerceIn(0.0, MAX_GPA) else 0.0
    return CgpaSummary(
      cgpa = cgpa,
      totalQualityPoints = totalQp,
      totalCreditHours = totalCredits,
      totalEarnedCreditHours = totalEarned,
      includedSemesterCount = includedSemesters.size,
      hasPriorBaseline = usePrior,
    )
  }

  fun calculateTargetPlan(
    currentQualityPoints: Double,
    currentCreditHours: Double,
    upcomingCreditHours: Double,
    targetCgpa: Double,
  ): TargetPlanResult {
    if (upcomingCreditHours <= 0.0) {
      val current =
        if (currentCreditHours > 0.0) currentQualityPoints / currentCreditHours else 0.0
      return TargetPlanResult(
        isReachable = current >= targetCgpa,
        requiredNextGpa = 0.0,
        projectedMaxCgpa = current,
        projectedMinCgpa = current,
      )
    }
    val totalCredits = currentCreditHours + upcomingCreditHours
    val neededTotalQp = targetCgpa * totalCredits
    val requiredQp = neededTotalQp - currentQualityPoints
    val requiredNextGpa = requiredQp / upcomingCreditHours
    val maxCgpa = (currentQualityPoints + MAX_GPA * upcomingCreditHours) / totalCredits
    val minCgpa = currentQualityPoints / totalCredits
    return TargetPlanResult(
      isReachable = requiredNextGpa <= MAX_GPA + 1e-6,
      requiredNextGpa = max(0.0, requiredNextGpa),
      projectedMaxCgpa = maxCgpa.coerceIn(0.0, MAX_GPA),
      projectedMinCgpa = minCgpa.coerceIn(0.0, MAX_GPA),
    )
  }

  fun standingLabel(gpa: Double): String =
    when {
      gpa >= 3.70 -> "Dean's Distinction"
      gpa >= 3.30 -> "Honors Standing"
      gpa >= 2.50 -> "Good Standing"
      gpa >= 2.00 -> "Satisfactory Pass"
      gpa > 0.0 -> "Academic Probation"
      else -> "No Graded Courses"
    }
}
