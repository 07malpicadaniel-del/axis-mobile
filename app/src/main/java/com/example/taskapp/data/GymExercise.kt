package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gym_exercises")
data class GymExercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1 = Lunes, ..., 7 = Domingo
    val name: String, // e.g., "Press de Banca"
    val setsReps: String = "4x10", // e.g., "4 series x 10 reps"
    val category: String = "General", // e.g., "Pecho", "Espalda", "Pierna", "Hombros", "Brazos", "Cardio"
    val isCompleted: Boolean = false
)
