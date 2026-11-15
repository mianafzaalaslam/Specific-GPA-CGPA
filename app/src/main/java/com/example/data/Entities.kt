package com.example.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "semesters")
data class SemesterEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val title: String,
  val sortOrder: Int = 0,
  val isSummaryOnly: Boolean = false,
  val summaryGpa: Double = 0.0,
  val summaryCredits: Double = 0.0,
  val includedInCgpa: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
  tableName = "courses",
  foreignKeys =
    [
      ForeignKey(
        entity = SemesterEntity::class,
        parentColumns = ["id"],
        childColumns = ["semesterId"],
        onDelete = ForeignKey.CASCADE,
      )
    ],
  indices = [Index("semesterId")],
)
data class CourseEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val semesterId: Long,
  val name: String,
  val creditHours: Double,
  val status: String,
  val entryMode: String,
  val directPercent: Double,
  val midMarks: Double? = null,
  val sessionalMarks: Double? = null,
  val finalMarks: Double? = null,
  val maxMarks: Double = 100.0,
  val sortOrder: Int = 0,
)

@Entity(tableName = "prior_academic")
data class PriorAcademicEntity(
  @PrimaryKey val id: Int = 1,
  val enabled: Boolean = false,
  val priorCgpa: Double = 0.0,
  val priorCreditHours: Double = 0.0,
)

data class SemesterWithCourses(
  @Embedded val semester: SemesterEntity,
  @Relation(parentColumn = "id", entityColumn = "semesterId") val courses: List<CourseEntity>,
)
