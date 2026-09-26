package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_notes")
data class DailyNote(
    @PrimaryKey
    val dateMillis: Long, // Start of day timestamp
    val moodEmoji: String = "Great", // e.g., "Great", "Good", "Okay", "Tired"
    val noteText: String = ""
)
