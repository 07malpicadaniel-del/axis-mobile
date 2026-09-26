package com.example.taskapp.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskapp.data.*
import com.example.taskapp.util.NotificationHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class FilterStatus(val label: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Completed")
}

enum class RoutineType(val title: String, val description: String) {
    OFICINA("Office & Work Routine", "Structured around focused work, meal breaks, and exercise."),
    ESTUDIANTE("Student Routine", "Classes, deep study blocks, sports, and rest."),
    FIN_DE_SEMANA("Weekend Routine", "Hobbies, family time, outdoor activities, and leisure.")
}

data class DailyPerformanceHistory(
    val dateMillis: Long,
    val formattedDate: String,
    val completedTasks: Int,
    val totalTasks: Int,
    val completedHabits: Int,
    val totalExpenses: Double,
    val mood: String
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val isDarkTheme = MutableStateFlow(false)

    val selectedDateMillis = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))

    val allTasks: StateFlow<List<Task>>

    // Filtering states
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val selectedFilterStatus = MutableStateFlow(FilterStatus.ALL)

    // Flow of tasks filtered by search, category, and completion status
    val filteredTasks: StateFlow<List<Task>>

    // Flow of tasks grouped by start of day (timestamp) for quick calendar indicators
    val tasksGroupedByDate: StateFlow<Map<Long, List<Task>>>

    val tasksForSelectedDate: StateFlow<List<Task>>

    // Schedule / Horario Personal States
    val selectedScheduleDay = MutableStateFlow(1) // 1 = Mon, ..., 7 = Sun
    val scheduleSlotsForSelectedDay: StateFlow<List<ScheduleSlot>>
    val allScheduleSlots: StateFlow<List<ScheduleSlot>>

    // Gym / Workout Routine States
    val allGymExercises: StateFlow<List<GymExercise>>
    val exercisesForSelectedDay: StateFlow<List<GymExercise>>

    // Finance States
    val monthlyBudget = MutableStateFlow(1000.0) // Monthly income/budget limit (e.g. $1000)
    val allFinanceEntries: StateFlow<List<FinanceEntry>>
    val allSavingGoals: StateFlow<List<SavingGoal>>

    // Habit States
    val allHabits: StateFlow<List<Habit>>
    val allHabitLogs: StateFlow<List<HabitLog>>

    // Daily Note State
    val currentDailyNote: StateFlow<DailyNote?>

    // Historical Performance Log Flow (Past 7 Days)
    val historicalPerformance: StateFlow<List<DailyPerformanceHistory>>

    // Overall Active Daily Streak
    val overallDailyStreak: StateFlow<Int>

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(
            database.taskDao(),
            database.scheduleDao(),
            database.financeDao(),
            database.habitDao(),
            database.dailyNoteDao(),
            database.savingGoalDao(),
            database.gymExerciseDao()
        )

        allTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        filteredTasks = combine(
            allTasks,
            searchQuery,
            selectedCategory,
            selectedFilterStatus
        ) { tasks, query, category, status ->
            tasks.filter { task ->
                val matchesQuery = query.isBlank() ||
                        task.title.contains(query, ignoreCase = true) ||
                        task.description.contains(query, ignoreCase = true)

                val matchesCategory = (category == "All" || task.category.equals(category, ignoreCase = true))

                val matchesStatus = when (status) {
                    FilterStatus.ALL -> true
                    FilterStatus.PENDING -> !task.isCompleted
                    FilterStatus.COMPLETED -> task.isCompleted
                }

                matchesQuery && matchesCategory && matchesStatus
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        tasksGroupedByDate = allTasks.map { tasks ->
            tasks.groupBy { getStartOfDay(it.dateMillis) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

        tasksForSelectedDate = selectedDateMillis.flatMapLatest { dateMillis ->
            val startOfDay = getStartOfDay(dateMillis)
            val endOfDay = getEndOfDay(dateMillis)
            repository.getTasksByDateRange(startOfDay, endOfDay)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allScheduleSlots = repository.allScheduleSlots.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        scheduleSlotsForSelectedDay = selectedScheduleDay.flatMapLatest { dayOfWeek ->
            repository.getScheduleSlotsForDay(dayOfWeek)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allGymExercises = repository.allGymExercises.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        exercisesForSelectedDay = selectedScheduleDay.flatMapLatest { dayOfWeek ->
            repository.getExercisesForDay(dayOfWeek)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allFinanceEntries = repository.allFinanceEntries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSavingGoals = repository.allSavingGoals.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allHabits = repository.allHabits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allHabitLogs = repository.allHabitLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        currentDailyNote = selectedDateMillis.flatMapLatest { millis ->
            repository.getNoteForDate(getStartOfDay(millis))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        // Calculate 7-day Historical Performance
        historicalPerformance = combine(
            allTasks,
            allHabitLogs,
            allFinanceEntries,
            repository.allDailyNotes
        ) { tasks, habitLogs, finances, notes ->
            val historyList = mutableListOf<DailyPerformanceHistory>()
            val cal = Calendar.getInstance().apply { timeInMillis = getStartOfDay(System.currentTimeMillis()) }
            val dateFormat = SimpleDateFormat("MMM d", Locale.US)

            for (i in 0 until 7) {
                val dayStart = cal.timeInMillis
                val dayEnd = getEndOfDay(dayStart)

                val tasksForDay = tasks.filter { it.dateMillis in dayStart..dayEnd }
                val completedTasksCount = tasksForDay.count { it.isCompleted }

                val habitsCompletedCount = habitLogs.count { getStartOfDay(it.dateMillis) == dayStart }

                val dayExpenses = finances
                    .filter { it.type == TransactionType.EXPENSE && it.dateMillis in dayStart..dayEnd }
                    .sumOf { it.amount }

                val dayNote = notes.firstOrNull { getStartOfDay(it.dateMillis) == dayStart }
                val mood = dayNote?.moodEmoji ?: "-"

                historyList.add(
                    DailyPerformanceHistory(
                        dateMillis = dayStart,
                        formattedDate = dateFormat.format(Date(dayStart)),
                        completedTasks = completedTasksCount,
                        totalTasks = tasksForDay.size,
                        completedHabits = habitsCompletedCount,
                        totalExpenses = dayExpenses,
                        mood = mood
                    )
                )

                cal.add(Calendar.DAY_OF_YEAR, -1)
            }

            historyList
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Overall Streak Calculation
        overallDailyStreak = combine(allTasks, allHabitLogs) { tasks, habitLogs ->
            val completedDates = mutableSetOf<Long>()
            tasks.filter { it.isCompleted }.forEach { completedDates.add(getStartOfDay(it.dateMillis)) }
            habitLogs.forEach { completedDates.add(getStartOfDay(it.dateMillis)) }

            var streak = 0
            val cal = Calendar.getInstance().apply { timeInMillis = getStartOfDay(System.currentTimeMillis()) }

            while (true) {
                val dateToCheck = cal.timeInMillis
                if (completedDates.contains(dateToCheck)) {
                    streak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    if (streak == 0) {
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        if (completedDates.contains(cal.timeInMillis)) {
                            streak++
                            cal.add(Calendar.DAY_OF_YEAR, -1)
                            continue
                        }
                    }
                    break
                }
            }
            streak
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

        // Automated Notification Check for Pending Tasks Today
        viewModelScope.launch {
            val todayStart = getStartOfDay(System.currentTimeMillis())
            val todayEnd = getEndOfDay(todayStart)
            repository.getTasksByDateRange(todayStart, todayEnd).collect { todayTasks ->
                val pendingToday = todayTasks.filter { !it.isCompleted }
                if (pendingToday.isNotEmpty()) {
                    NotificationHelper.sendNotification(
                        getApplication(),
                        "Daily Task Reminder",
                        "You have ${pendingToday.size} pending task(s) scheduled for today."
                    )
                }
            }
        }
    }

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun selectDate(millis: Long) {
        selectedDateMillis.value = getStartOfDay(millis)
    }

    fun addTask(
        title: String,
        description: String,
        dateMillis: Long,
        priority: Priority = Priority.MEDIUM,
        category: String = "General"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val newTask = Task(
                title = title.trim(),
                description = description.trim(),
                dateMillis = dateMillis,
                isCompleted = false,
                priority = priority,
                category = category.trim().ifBlank { "General" }
            )
            repository.insertTask(newTask)

            NotificationHelper.sendNotification(
                getApplication(),
                "Task Scheduled",
                "Reminder set for: ${newTask.title}"
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    // Schedule Slot Methods
    fun selectScheduleDay(dayOfWeek: Int) {
        selectedScheduleDay.value = dayOfWeek
    }

    fun addScheduleSlot(
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        title: String,
        category: String = "General",
        colorHex: String = "#212121"
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val slot = ScheduleSlot(
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                title = title.trim(),
                category = category.trim().ifBlank { "General" },
                colorHex = colorHex
            )
            repository.insertScheduleSlot(slot)
        }
    }

    fun updateScheduleSlot(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.updateScheduleSlot(slot)
        }
    }

    fun deleteScheduleSlot(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.deleteScheduleSlot(slot)
        }
    }

    fun toggleScheduleSlotCompleted(slot: ScheduleSlot) {
        viewModelScope.launch {
            repository.updateScheduleSlot(slot.copy(isCompleted = !slot.isCompleted))
        }
    }

    // Gym Exercise Methods
    fun addGymExercise(dayOfWeek: Int, name: String, setsReps: String = "4x10", category: String = "Chest") {
        if (name.isBlank()) return
        viewModelScope.launch {
            val exercise = GymExercise(
                dayOfWeek = dayOfWeek,
                name = name.trim(),
                setsReps = setsReps.trim().ifBlank { "4x10" },
                category = category.trim().ifBlank { "Chest" },
                isCompleted = false
            )
            repository.insertGymExercise(exercise)
        }
    }

    fun toggleGymExerciseCompleted(exercise: GymExercise) {
        viewModelScope.launch {
            repository.updateGymExercise(exercise.copy(isCompleted = !exercise.isCompleted))
        }
    }

    fun deleteGymExercise(exercise: GymExercise) {
        viewModelScope.launch {
            repository.deleteGymExercise(exercise)
        }
    }

    fun applyPredefinedGymRoutine(dayOfWeek: Int, routineFocus: String) {
        viewModelScope.launch {
            val exercises = when (routineFocus) {
                "Chest & Triceps" -> listOf(
                    GymExercise(dayOfWeek = dayOfWeek, name = "Flat Barbell Bench Press", setsReps = "4 sets x 10 reps", category = "Chest"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Incline Dumbbell Press", setsReps = "4 sets x 12 reps", category = "Chest"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Cable Flyes", setsReps = "3 sets x 15 reps", category = "Chest"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Triceps Pushdowns", setsReps = "4 sets x 12 reps", category = "Triceps"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Parallel Bar Dips", setsReps = "3 sets x failure", category = "Triceps")
                )
                "Back & Biceps" -> listOf(
                    GymExercise(dayOfWeek = dayOfWeek, name = "Pull-ups", setsReps = "4 sets x 8 reps", category = "Back"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Barbell Bent-Over Row", setsReps = "4 sets x 10 reps", category = "Back"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Lat Pulldowns", setsReps = "3 sets x 12 reps", category = "Back"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Barbell Biceps Curl", setsReps = "4 sets x 10 reps", category = "Biceps"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Hammer Curls", setsReps = "3 sets x 12 reps", category = "Biceps")
                )
                "Legs & Shoulders" -> listOf(
                    GymExercise(dayOfWeek = dayOfWeek, name = "Barbell Back Squat", setsReps = "4 sets x 10 reps", category = "Legs"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Leg Press", setsReps = "4 sets x 12 reps", category = "Legs"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Romanian Deadlift", setsReps = "3 sets x 10 reps", category = "Legs"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Seated Overhead Press", setsReps = "4 sets x 10 reps", category = "Shoulders"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Lateral Raises", setsReps = "4 sets x 15 reps", category = "Shoulders")
                )
                else -> listOf(
                    GymExercise(dayOfWeek = dayOfWeek, name = "Treadmill Run / Cardio", setsReps = "20-30 mins", category = "Cardio"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Abdominal Crunches", setsReps = "4 sets x 20 reps", category = "Abs"),
                    GymExercise(dayOfWeek = dayOfWeek, name = "Plank Hold", setsReps = "3 sets x 60 secs", category = "Abs")
                )
            }

            exercises.forEach { ex ->
                repository.insertGymExercise(ex)
            }
        }
    }

    // Predefined Routines Generator
    fun applyPredefinedRoutine(dayOfWeek: Int, routineType: RoutineType) {
        viewModelScope.launch {
            val presetSlots = when (routineType) {
                RoutineType.OFICINA -> listOf(
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "07:00", endTime = "08:00", title = "Workout & Breakfast", category = "Fitness", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "08:30", endTime = "13:00", title = "Deep Work Block", category = "Work", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "13:00", endTime = "14:00", title = "Lunch & Rest", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "14:00", endTime = "18:00", title = "Work & Meetings", category = "Work", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "18:30", endTime = "20:00", title = "Gym / Sports", category = "Fitness", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "20:30", endTime = "22:30", title = "Free Time & Dinner", category = "Personal", colorHex = "#212121")
                )
                RoutineType.ESTUDIANTE -> listOf(
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "07:30", endTime = "08:30", title = "Morning Prep & Coffee", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "08:30", endTime = "12:30", title = "Lectures & Study", category = "Study", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "12:30", endTime = "13:30", title = "Lunch Break", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "14:00", endTime = "17:00", title = "Projects & Homework", category = "Study", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "17:30", endTime = "19:00", title = "Outdoor Walk / Run", category = "Fitness", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "19:30", endTime = "22:00", title = "Reading & Unwind", category = "Personal", colorHex = "#212121")
                )
                RoutineType.FIN_DE_SEMANA -> listOf(
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "09:00", endTime = "10:00", title = "Late Breakfast & Relax", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "10:30", endTime = "12:30", title = "Hobbies / Side Projects", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "13:00", endTime = "15:00", title = "Family Meal", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "15:30", endTime = "18:30", title = "Walk / Free Time", category = "Personal", colorHex = "#212121"),
                    ScheduleSlot(dayOfWeek = dayOfWeek, startTime = "19:00", endTime = "22:00", title = "Movie / Evening Relaxation", category = "Personal", colorHex = "#212121")
                )
            }

            presetSlots.forEach { slot ->
                repository.insertScheduleSlot(slot)
            }
        }
    }

    // Finance Methods
    fun setMonthlyBudget(budget: Double) {
        if (budget > 0) {
            monthlyBudget.value = budget
        }
    }

    fun addFinanceEntry(
        title: String,
        amount: Double,
        type: TransactionType,
        category: String,
        dateMillis: Long = System.currentTimeMillis()
    ) {
        if (title.isBlank() || amount <= 0) return
        viewModelScope.launch {
            val entry = FinanceEntry(
                title = title.trim(),
                amount = amount,
                type = type,
                category = category.trim().ifBlank { "General" },
                dateMillis = dateMillis
            )
            repository.insertFinanceEntry(entry)
        }
    }

    fun updateFinanceEntry(entry: FinanceEntry) {
        viewModelScope.launch {
            repository.updateFinanceEntry(entry)
        }
    }

    fun deleteFinanceEntry(entry: FinanceEntry) {
        viewModelScope.launch {
            repository.deleteFinanceEntry(entry)
        }
    }

    // Habit Methods
    fun addHabit(title: String, category: String = "General", colorHex: String = "#212121") {
        if (title.isBlank()) return
        viewModelScope.launch {
            val habit = Habit(title = title.trim(), category = category, colorHex = colorHex)
            repository.insertHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitForDate(habitId: Long, dateMillis: Long, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            val dayStart = getStartOfDay(dateMillis)
            if (isCurrentlyCompleted) {
                repository.deleteHabitLog(habitId, dayStart)
            } else {
                repository.insertHabitLog(HabitLog(habitId = habitId, dateMillis = dayStart))
            }
        }
    }

    fun getHabitStreak(habitId: Long, logs: List<HabitLog>): Int {
        val habitLogsDates = logs
            .filter { it.habitId == habitId }
            .map { getStartOfDay(it.dateMillis) }
            .toSet()

        var streak = 0
        val cal = Calendar.getInstance().apply { timeInMillis = getStartOfDay(System.currentTimeMillis()) }

        while (true) {
            val dateToCheck = cal.timeInMillis
            if (habitLogsDates.contains(dateToCheck)) {
                streak++
                cal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                if (streak == 0) {
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                    if (habitLogsDates.contains(cal.timeInMillis)) {
                        streak++
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                        continue
                    }
                }
                break
            }
        }
        return streak
    }

    // Daily Note Methods
    fun saveDailyNote(moodEmoji: String, text: String) {
        viewModelScope.launch {
            val note = DailyNote(
                dateMillis = selectedDateMillis.value,
                moodEmoji = moodEmoji,
                noteText = text.trim()
            )
            repository.insertDailyNote(note)
        }
    }

    // Saving Goal Methods
    fun addSavingGoal(title: String, targetAmount: Double, colorHex: String = "#212121") {
        if (title.isBlank() || targetAmount <= 0) return
        viewModelScope.launch {
            val goal = SavingGoal(title = title.trim(), targetAmount = targetAmount, colorHex = colorHex)
            repository.insertSavingGoal(goal)
        }
    }

    fun depositToSavingGoal(goal: SavingGoal, depositAmount: Double) {
        if (depositAmount <= 0) return
        viewModelScope.launch {
            val updatedGoal = goal.copy(currentAmount = goal.currentAmount + depositAmount)
            repository.updateSavingGoal(updatedGoal)

            // Also add a finance entry for the savings deposit
            val entry = FinanceEntry(
                title = "Savings: ${goal.title}",
                amount = depositAmount,
                type = TransactionType.EXPENSE,
                category = "Savings",
                dateMillis = System.currentTimeMillis()
            )
            repository.insertFinanceEntry(entry)
        }
    }

    fun deleteSavingGoal(goal: SavingGoal) {
        viewModelScope.launch {
            repository.deleteSavingGoal(goal)
        }
    }

    companion object {
        val categories = listOf("All", "General", "Work", "Personal", "Study", "Home")
        val financeCategories = listOf("Rent", "Groceries", "Utilities", "Transport", "Leisure", "Savings", "Other")

        fun getStartOfDay(timeMillis: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }

        fun getEndOfDay(timeMillis: Long): Long {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timeMillis
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            return calendar.timeInMillis
        }
    }
}

class TaskViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
