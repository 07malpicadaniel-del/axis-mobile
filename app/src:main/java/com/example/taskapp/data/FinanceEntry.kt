package com.example.taskapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    INCOME("Income"),
    EXPENSE("Expense")
}

@Entity(tableName = "finance_entries")
data class FinanceEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: String, // e.g., "Rent", "Groceries", "Utilities", "Transport", "Leisure", "Savings", "Other"
    val dateMillis: Long
)
