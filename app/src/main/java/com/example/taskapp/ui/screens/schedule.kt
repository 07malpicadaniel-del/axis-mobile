package com.example.taskapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.GymExercise
import com.example.taskapp.data.ScheduleSlot
import com.example.taskapp.ui.viewmodel.RoutineType
import com.example.taskapp.ui.viewmodel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: TaskViewModel) {
    val selectedDay by viewModel.selectedScheduleDay.collectAsState()
    val scheduleSlots by viewModel.scheduleSlotsForSelectedDay.collectAsState()
    val gymExercises by viewModel.exercisesForSelectedDay.collectAsState()

    var activeSubSection by remember { mutableIntStateOf(0) } // 0 = Schedule, 1 = Gym

    var showDialog by remember { mutableStateOf(false) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var showGymDialog by remember { mutableStateOf(false) }
    var showPresetGymDialog by remember { mutableStateOf(false) }

    var slotToEdit by remember { mutableStateOf<ScheduleSlot?>(null) }

    val daysOfWeek = listOf(
        Pair(1, "Monday"),
        Pair(2, "Tuesday"),
        Pair(3, "Wednesday"),
        Pair(4, "Thursday"),
        Pair(5, "Friday"),
        Pair(6, "Saturday"),
        Pair(7, "Sunday")
    )

    val completedCount = scheduleSlots.count { it.isCompleted }
    val totalCount = scheduleSlots.size

    val completedGymCount = gymExercises.count { it.isCompleted }
    val totalGymCount = gymExercises.size
    val gymProgress = if (totalGymCount > 0) (completedGymCount.toFloat() / totalGymCount) else 0f

    Scaffold(
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (activeSubSection == 0) {
                    SmallFloatingActionButton(
                        onClick = { showRoutineDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Load Preset Routine")
                    }

                    FloatingActionButton(
                        onClick = {
                            slotToEdit = null
                            showDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Schedule Block")
                    }
                } else {
                    SmallFloatingActionButton(
                        onClick = { showPresetGymDialog = true },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Load Gym Routine")
                    }

                    FloatingActionButton(
                        onClick = { showGymDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Gym Exercise")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Day Selector Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedDay - 1,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                daysOfWeek.forEach { (dayNum, dayName) ->
                    Tab(
                        selected = selectedDay == dayNum,
                        onClick = { viewModel.selectScheduleDay(dayNum) },
                        text = { Text(dayName, fontWeight = if (selectedDay == dayNum) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            // Subsection Switcher
            SecondaryTabRow(
                selectedTabIndex = activeSubSection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeSubSection == 0,
                    onClick = { activeSubSection = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Time Schedule")
                        }
                    }
                )
                Tab(
                    selected = activeSubSection == 1,
                    onClick = { activeSubSection = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Workout & Gym")
                        }
                    }
                )
            }

            if (activeSubSection == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Daily Schedule", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        if (totalCount > 0) {
                            Text("$completedCount of $totalCount completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    OutlinedButton(onClick = { showRoutineDialog = true }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Load Routine", fontSize = 12.sp)
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (scheduleSlots.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No activities for this day.\nTap 'Load Routine' or + to organize.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(scheduleSlots, key = { it.id }) { slot ->
                                ScheduleSlotCard(
                                    slot = slot,
                                    onToggleComplete = { viewModel.toggleScheduleSlotCompleted(slot) },
                                    onEdit = {
                                        slotToEdit = slot
                                        showDialog = true
                                    },
                                    onDelete = { viewModel.deleteScheduleSlot(slot) }
                                )
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Today's Workout Routine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("$completedGymCount of $totalGymCount exercises completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            OutlinedButton(onClick = { showPresetGymDialog = true }) {
                                Text("Load Gym", fontSize = 11.sp)
                            }
                        }

                        if (totalGymCount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { gymProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (gymExercises.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No exercises added for today.\nTap + or 'Load Gym' to start.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(gymExercises, key = { it.id }) { exercise ->
                                GymExerciseCard(
                                    exercise = exercise,
                                    onToggleComplete = { viewModel.toggleGymExerciseCompleted(exercise) },
                                    onDelete = { viewModel.deleteGymExercise(exercise) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPresetGymDialog) {
        PresetGymRoutineDialog(
            onDismiss = { showPresetGymDialog = false },
            onSelectFocus = { focus ->
                viewModel.applyPredefinedGymRoutine(selectedDay, focus)
                showPresetGymDialog = false
            }
        )
    }

    if (showGymDialog) {
        AddGymExerciseDialog(
            onDismiss = { showGymDialog = false },
            onConfirm = { name, setsReps, category ->
                viewModel.addGymExercise(selectedDay, name, setsReps, category)
                showGymDialog = false
            }
        )
    }

    if (showRoutineDialog) {
        RoutinePresetDialog(
            onDismiss = { showRoutineDialog = false },
            onSelectRoutine = { routineType ->
                viewModel.applyPredefinedRoutine(selectedDay, routineType)
                showRoutineDialog = false
            }
        )
    }

    if (showDialog) {
        ScheduleSlotDialog(
            slot = slotToEdit,
            defaultDayOfWeek = selectedDay,
            onDismiss = { showDialog = false },
            onConfirm = { dayOfWeek, startTime, endTime, title, category, colorHex ->
                if (slotToEdit == null) {
                    viewModel.addScheduleSlot(dayOfWeek, startTime, endTime, title, category, colorHex)
                } else {
                    viewModel.updateScheduleSlot(
                        slotToEdit!!.copy(
                            dayOfWeek = dayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            title = title,
                            category = category,
                            colorHex = colorHex
                        )
                    )
                }
                showDialog = false
            }
        )
    }
}

@Composable
fun GymExerciseCard(
    exercise: GymExercise,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (exercise.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = exercise.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (exercise.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (exercise.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = exercise.setsReps,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = exercise.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Exercise",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun PresetGymRoutineDialog(
    onDismiss: () -> Unit,
    onSelectFocus: (String) -> Unit
) {
    val routineOptions = listOf("Chest & Triceps", "Back & Biceps", "Legs & Shoulders", "Cardio & Abs")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Gym Routine") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select workout focus for today:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))

                routineOptions.forEach { focus ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectFocus(focus) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = focus,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddGymExerciseDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, setsReps: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var setsReps by remember { mutableStateOf("4 sets x 10 reps") }
    var category by remember { mutableStateOf("Chest") }

    val categories = listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Cardio", "Abs")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Gym Exercise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name (e.g. Bench Press)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = setsReps,
                    onValueChange = { setsReps = it },
                    label = { Text("Sets & Reps (e.g. 4x10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Muscle Group:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, setsReps, category) },
                enabled = name.isNotBlank()
            ) {
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
fun ScheduleSlotCard(
    slot: ScheduleSlot,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = slot.isCompleted,
                onCheckedChange = { onToggleComplete() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${slot.startTime} - ${slot.endTime}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = slot.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (slot.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (slot.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = slot.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Schedule")
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Schedule",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun RoutinePresetDialog(
    onDismiss: () -> Unit,
    onSelectRoutine: (RoutineType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Load Preset Routine") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select a preset routine for the selected day:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                RoutineType.entries.forEach { routine ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRoutine(routine) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(routine.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(routine.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSlotDialog(
    slot: ScheduleSlot? = null,
    defaultDayOfWeek: Int = 1,
    onDismiss: () -> Unit,
    onConfirm: (dayOfWeek: Int, startTime: String, endTime: String, title: String, category: String, colorHex: String) -> Unit
) {
    var title by remember { mutableStateOf(slot?.title ?: "") }
    var startTime by remember { mutableStateOf(slot?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(slot?.endTime ?: "09:00") }
    var category by remember { mutableStateOf(slot?.category ?: "General") }
    var selectedDay by remember { mutableIntStateOf(slot?.dayOfWeek ?: defaultDayOfWeek) }
    var selectedColorHex by remember { mutableStateOf(slot?.colorHex ?: "#000000") }

    val daysMap = listOf(
        Pair(1, "Mon"),
        Pair(2, "Tue"),
        Pair(3, "Wed"),
        Pair(4, "Thu"),
        Pair(5, "Fri"),
        Pair(6, "Sat"),
        Pair(7, "Sun")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (slot == null) "New Schedule Block" else "Edit Schedule Block") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Activity Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start (e.g. 08:00)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End (e.g. 09:30)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Day of Week:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(daysMap) { (dayNum, dayLabel) ->
                        FilterChip(
                            selected = selectedDay == dayNum,
                            onClick = { selectedDay = dayNum },
                            label = { Text(dayLabel) }
                        )
                    }
                }

                Text("Category:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(listOf("General", "Work", "Study", "Fitness", "Personal")) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDay, startTime, endTime, title, category, selectedColorHex) },
                enabled = title.isNotBlank()
            ) {
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
