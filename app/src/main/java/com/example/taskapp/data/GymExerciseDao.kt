package com.example.taskapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GymExerciseDao {
    @Query("SELECT * FROM gym_exercises ORDER BY dayOfWeek ASC, id ASC")
    fun getAllExercises(): Flow<List<GymExercise>>

    @Query("SELECT * FROM gym_exercises WHERE dayOfWeek = :dayOfWeek ORDER BY id ASC")
    fun getExercisesForDay(dayOfWeek: Int): Flow<List<GymExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: GymExercise)

    @Update
    suspend fun updateExercise(exercise: GymExercise)

    @Delete
    suspend fun deleteExercise(exercise: GymExercise)
}
