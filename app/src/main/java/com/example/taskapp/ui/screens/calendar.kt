package com.example.taskapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.Priority
import com.example.taskapp.data.Task
import com.example.taskapp.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(viewModel: TaskViewModel) {
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsState()
    val tasksGroupedByDate by viewModel.tasksGroupedByDate.collectAsState()
    val currentDailyNote by viewModel.currentDailyNote.collectAsState()

    var showNoteDialog by remember { mutableStateOf(false) }

    var currentMonthCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply { timeInMillis = selectedDateMillis })
    }

    val selectedDateHeaderFormat = remember {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    }

    val pendingCount = tasksForDate.count { !it.isCompleted }
    val completedCount = tasksForDate.count { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Interactive Month Calendar Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                MonthHeader(
                    currentMonth = currentMonthCalendar,
                    onPreviousMonth = {
                        currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    },
                    onNextMonth = {
                        currentMonthCalendar = (currentMonthCalendar.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    },
                    onTodayClick = {
                        val todayMillis = System.currentTimeMillis()
                        currentMonthCalendar = Calendar.getInstance().apply { timeInMillis = todayMillis }
                        viewModel.selectDate(todayMillis)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeekdayHeader()

                Spacer(modifier = Modifier.height(4.dp))

                MonthDaysGrid(
                    displayedMonth = currentMonthCalendar,
                    selectedDateMillis = selectedDateMillis,
                    tasksGroupedByDate = tasksGroupedByDate,
                    onDateSelected = { dateMillis ->
                        viewModel.selectDate(dateMillis)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selected Date Summary Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedDateHeaderFormat.format(Date(selectedDateMillis)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$pendingCount pending • $completedCount completed",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Daily Mood & Journal Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Mood, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Daily Mood:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = currentDailyNote?.moodEmoji ?: "Great",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (!currentDailyNote?.noteText.isNullOrEmpty()) currentDailyNote!!.noteText else "Tap edit to write a daily reflection.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                    }
                }

                IconButton(onClick = { showNoteDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Note")
                }
            }
        }
    }

    if (showNoteDialog) {
        DailyNoteDialog(
            initialMood = currentDailyNote?.moodEmoji ?: "Great",
            initialText = currentDailyNote?.noteText ?: "",
            onDismiss = { showNoteDialog = false },
            onConfirm = { mood, text ->
                viewModel.saveDailyNote(mood, text)
                showNoteDialog = false
            }
        )
    }
}

@Composable
fun DailyNoteDialog(
    initialMood: String,
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (moodEmoji: String, text: String) -> Unit
) {
    var selectedMood by remember { mutableStateOf(initialMood) }
    var noteText by remember { mutableStateOf(initialText) }

    val moodOptions = listOf("Great", "Good", "Okay", "Tired")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Reflection & Mood") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("How was your day?", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moodOptions.forEach { mood ->
                        FilterChip(
                            selected = selectedMood == mood,
                            onClick = { selectedMood = mood },
                            label = { Text(mood) }
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Daily Note") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedMood, noteText) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MonthHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTodayClick: () -> Unit
) {
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }
    val title = monthYearFormat.format(currentMonth.time)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onTodayClick) {
                Icon(Icons.Default.Today, contentDescription = "Today")
            }
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
            }
        }
    }
}

@Composable
fun WeekdayHeader() {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    Row(modifier = Modifier.fillMaxWidth()) {
        days.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthDaysGrid(
    displayedMonth: Calendar,
    selectedDateMillis: Long,
    tasksGroupedByDate: Map<Long, List<Task>>,
    onDateSelected: (Long) -> Unit
) {
    val daysInMonth = remember(displayedMonth) {
        val cal = displayedMonth.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = mutableListOf<CalendarDay?>()
        for (i in 0 until firstDayOfWeek) {
            list.add(null)
        }

        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dayStartMillis = TaskViewModel.getStartOfDay(cal.timeInMillis)
            list.add(CalendarDay(dayNumber = day, startOfDayMillis = dayStartMillis))
        }
        list
    }

    val todayStartMillis = remember { TaskViewModel.getStartOfDay(System.currentTimeMillis()) }
    val selectedStartMillis = remember(selectedDateMillis) { TaskViewModel.getStartOfDay(selectedDateMillis) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        userScrollEnabled = false
    ) {
        items(daysInMonth) { day ->
            if (day == null) {
                Box(modifier = Modifier.size(38.dp))
            } else {
                val isSelected = day.startOfDayMillis == selectedStartMillis
                val isToday = day.startOfDayMillis == todayStartMillis
                val dayTasks = tasksGroupedByDate[day.startOfDayMillis] ?: emptyList()

                DayCell(
                    dayNumber = day.dayNumber,
                    isSelected = isSelected,
                    isToday = isToday,
                    tasks = dayTasks,
                    onClick = { onDateSelected(day.startOfDayMillis) }
                )
            }
        }
    }
}

data class CalendarDay(val dayNumber: Int, val startOfDayMillis: Long)

@Composable
fun DayCell(
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    tasks: List<Task>,
    onClick: () -> Unit
) {
    val pendingTasks = tasks.filter { !it.isCompleted }
    val hasTasks = tasks.isNotEmpty()
    val hasHighPriorityPending = pendingTasks.any { it.priority == Priority.HIGH }

    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier
            .padding(2.dp)
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .then(
                if (isToday && !isSelected) Modifier.border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                ) else Modifier
            )
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayNumber.toString(),
            fontSize = 13.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        if (hasTasks) {
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotColor = if (hasHighPriorityPending) MaterialTheme.colorScheme.error else if (isSelected) Color.White else MaterialTheme.colorScheme.primary
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}
