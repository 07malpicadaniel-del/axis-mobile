package com.example.taskapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskapp.data.Priority
import com.example.taskapp.data.TransactionType
import com.example.taskapp.ui.viewmodel.HistoryPeriod
import com.example.taskapp.ui.viewmodel.TaskViewModel
import java.util.Locale

@Composable
fun StatsScreen(viewModel: TaskViewModel) {
    val allTasks by viewModel.allTasks.collectAsState()
    val allFinanceEntries by viewModel.allFinanceEntries.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val allScheduleSlots by viewModel.allScheduleSlots.collectAsState()

    val selectedHistoryPeriod by viewModel.selectedHistoryPeriod.collectAsState()
    val periodComplianceHistory by viewModel.periodComplianceHistory.collectAsState()

    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("Overview", "Tasks", "Finances", "Schedule", "History")

    // Task Stats
    val totalTasks = allTasks.size
    val completedTasks = allTasks.count { it.isCompleted }
    val taskCompletionPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) * 100f else 0f
    val highPriorityPending = allTasks.count { !it.isCompleted && it.priority == Priority.HIGH }

    // Finance Stats
    val totalExpenses = allFinanceEntries.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val totalIncomeEntries = allFinanceEntries.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalBudgetAvailable = monthlyBudget + totalIncomeEntries
    val remainingBalance = totalBudgetAvailable - totalExpenses
    val spentPercentage = if (totalBudgetAvailable > 0) ((totalExpenses / totalBudgetAvailable) * 100).coerceAtMost(100.0) else 0.0

    val financeCategories = listOf("Rent", "Groceries", "Utilities", "Transport", "Leisure", "Savings", "Other")
    val expensesByCategory = financeCategories.associateWith { cat ->
        allFinanceEntries.filter { it.type == TransactionType.EXPENSE && it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
    }

    // Schedule Stats
    val totalScheduleSlots = allScheduleSlots.size
    val completedScheduleSlots = allScheduleSlots.count { it.isCompleted }
    val scheduleCompliancePercentage = if (totalScheduleSlots > 0) (completedScheduleSlots.toFloat() / totalScheduleSlots) * 100f else 0f

    val scheduleCategories = listOf("Work", "Study", "Fitness", "Personal")
    val slotsByCategory = scheduleCategories.associateWith { cat ->
        allScheduleSlots.count { it.category.equals(cat, ignoreCase = true) }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        // Scrollable Section Tabs (Prevents text wrapping onto multiple lines)
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedSectionIndex,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            sectionTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSectionIndex == index,
                    onClick = { selectedSectionIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedSectionIndex == index) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedSectionIndex) {
                0 -> {
                    // Visión General Dashboard
                    Text("Control Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Task Completion", value = "${taskCompletionPercentage.toInt()}%", subtitle = "$completedTasks of $totalTasks tasks", modifier = Modifier.weight(1f))
                        MetricCard(title = "Schedule Adherence", value = "${scheduleCompliancePercentage.toInt()}%", subtitle = "$completedScheduleSlots of $totalScheduleSlots done", modifier = Modifier.weight(1f))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Remaining Budget", value = "$${String.format(Locale.US, "%.0f", remainingBalance)}", subtitle = "${spentPercentage.toInt()}% used", modifier = Modifier.weight(1f))
                        MetricCard(title = "Critical Tasks", value = "$highPriorityPending", subtitle = "High priority pending", modifier = Modifier.weight(1f))
                    }

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Overall Balance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            PriorityProgressRow(title = "Tasks Completion", count = completedTasks, total = totalTasks)
                            Spacer(modifier = Modifier.height(8.dp))
                            PriorityProgressRow(title = "Schedule Adherence", count = completedScheduleSlots, total = totalScheduleSlots)
                            Spacer(modifier = Modifier.height(8.dp))
                            PriorityProgressRow(title = "Budget Spent", count = totalExpenses.toInt(), total = totalBudgetAvailable.toInt())
                        }
                    }
                }

                1 -> {
                    // Tareas
                    Text("Tasks Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Overall Task Progress", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 18.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                    drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                    val sweepAngle = (taskCompletionPercentage / 100f) * 360f
                                    drawArc(color = primaryColor, startAngle = -90f, sweepAngle = sweepAngle, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${taskCompletionPercentage.toInt()}%", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                                    Text("$completedTasks of $totalTasks completed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Priority Breakdown", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            val highCount = allTasks.count { it.priority == Priority.HIGH }
                            val mediumCount = allTasks.count { it.priority == Priority.MEDIUM }
                            val lowCount = allTasks.count { it.priority == Priority.LOW }

                            PriorityProgressRow(title = "High Priority", count = highCount, total = totalTasks)
                            PriorityProgressRow(title = "Medium Priority", count = mediumCount, total = totalTasks)
                            PriorityProgressRow(title = "Low Priority", count = lowCount, total = totalTasks)
                        }
                    }
                }

                2 -> {
                    // Finanzas
                    Text("Financial Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Budget / Income", value = "$${String.format(Locale.US, "%.0f", totalBudgetAvailable)}", subtitle = "Monthly total", modifier = Modifier.weight(1f))
                        MetricCard(title = "Total Expenses", value = "-$${String.format(Locale.US, "%.0f", totalExpenses)}", subtitle = "${spentPercentage.toInt()}% spent", modifier = Modifier.weight(1f))
                    }

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Expenses Breakdown by Category", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            val maxExpense = expensesByCategory.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

                            expensesByCategory.forEach { (cat, amount) ->
                                val fraction = (amount / maxExpense).toFloat()

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text("$${String.format(Locale.US, "%.2f", amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                        drawRoundRect(color = trackColor, size = Size(size.width, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        if (fraction > 0f) {
                                            drawRoundRect(color = primaryColor, size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Rutinas & Horario
                    Text("Schedule Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricCard(title = "Total Schedule Blocks", value = "$totalScheduleSlots", subtitle = "Weekly activities", modifier = Modifier.weight(1f))
                        MetricCard(title = "Schedule Adherence", value = "${scheduleCompliancePercentage.toInt()}%", subtitle = "$completedScheduleSlots done", modifier = Modifier.weight(1f))
                    }

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Routine Adherence Rate", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 18.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                                    drawArc(color = trackColor, startAngle = 0f, sweepAngle = 360f, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                    val sweepAngle = (scheduleCompliancePercentage / 100f) * 360f
                                    drawArc(color = primaryColor, startAngle = -90f, sweepAngle = sweepAngle, useCenter = false, topLeft = topLeft, size = Size(diameter, diameter), style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${scheduleCompliancePercentage.toInt()}%", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = primaryColor)
                                    Text("$completedScheduleSlots of $totalScheduleSlots done", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Text("Time Distribution by Category", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))

                            val maxSlots = slotsByCategory.values.maxOrNull()?.coerceAtLeast(1) ?: 1

                            slotsByCategory.forEach { (cat, count) ->
                                val fraction = count.toFloat() / maxSlots

                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(cat, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text("$count blocks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                        drawRoundRect(color = trackColor, size = Size(size.width, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        if (fraction > 0f) {
                                            drawRoundRect(color = primaryColor, size = Size(size.width * fraction, size.height), cornerRadius = CornerRadius(5.dp.toPx()))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // History Log with Period Selector (Weekly, Monthly, Yearly)
                    Text("Compliance History & Analytics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryColor)

                    // Period Selector Chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(HistoryPeriod.entries.toTypedArray()) { period ->
                            FilterChip(
                                selected = selectedHistoryPeriod == period,
                                onClick = { viewModel.selectedHistoryPeriod.value = period },
                                label = { Text(period.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (periodComplianceHistory.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                            Text("No historical compliance data available.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        periodComplianceHistory.forEach { history ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = history.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Task Compliance Bar
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Task Completion", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("${history.taskCompletionPercentage}% (${history.completedTasks}/${history.totalTasks})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (history.taskCompletionPercentage / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = primaryColor,
                                        trackColor = trackColor,
                                        strokeCap = StrokeCap.Round
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Habit Completion Bar
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Habit Completion", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("${history.habitCompletionPercentage}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { (history.habitCompletionPercentage / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(6.dp),
                                        color = primaryColor,
                                        trackColor = trackColor,
                                        strokeCap = StrokeCap.Round
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Expenses Total
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Period Expenses", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("-$${String.format(Locale.US, "%.2f", history.totalExpenses)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PriorityProgressRow(
    title: String,
    count: Int,
    total: Int
) {
    val fraction = if (total > 0) (count.toFloat() / total).coerceIn(0f, 1f) else 0f
    val percentage = (fraction * 100).toInt()

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("$count ($percentage%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}
