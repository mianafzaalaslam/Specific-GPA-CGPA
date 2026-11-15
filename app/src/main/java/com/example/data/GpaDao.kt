package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GpaDao {
  @Transaction
  @Query("SELECT * FROM semesters ORDER BY sortOrder ASC, id ASC")
  fun observeSemestersWithCourses(): Flow<List<SemesterWithCourses>>

  @Query("SELECT COUNT(*) FROM semesters")
  suspend fun getSemesterCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSemester(semester: SemesterEntity): Long

  @Update
  suspend fun updateSemester(semester: SemesterEntity)

  @Delete
  suspend fun deleteSemester(semester: SemesterEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCourse(course: CourseEntity): Long

  @Update
  suspend fun updateCourse(course: CourseEntity)

  @Delete
  suspend fun deleteCourse(course: CourseEntity)

  @Query("DELETE FROM courses WHERE id = :courseId")
  suspend fun deleteCourseById(courseId: Long)

  @Query("SELECT * FROM prior_academic WHERE id = 1")
  fun observePriorAcademic(): Flow<PriorAcademicEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPriorAcademic(prior: PriorAcademicEntity)
}
