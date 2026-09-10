package com.example.taskpulse.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.Task
import com.example.taskpulse.ui.theme.Amber
import com.example.taskpulse.ui.theme.AmberDim
import com.example.taskpulse.ui.theme.Fog
import com.example.taskpulse.ui.theme.Graphite
import com.example.taskpulse.ui.theme.GraphiteFaint
import com.example.taskpulse.ui.theme.Indigo
import com.example.taskpulse.ui.theme.IndigoDim
import com.example.taskpulse.ui.theme.Ink
import com.example.taskpulse.ui.theme.Line
import com.example.taskpulse.ui.theme.Moss
import com.example.taskpulse.ui.theme.MossDim
import com.example.taskpulse.ui.theme.Ochre
import com.example.taskpulse.ui.theme.OchreDim
import com.example.taskpulse.ui.theme.Panel
import com.example.taskpulse.ui.theme.PanelRaised
import com.example.taskpulse.ui.theme.Paper
import com.example.taskpulse.ui.theme.PaperShade
import com.example.taskpulse.ui.theme.Pulse
import com.example.taskpulse.ui.theme.PulseDim
import com.example.taskpulse.ui.theme.Rule
import com.example.taskpulse.ui.theme.TaskPulseTheme
import com.example.taskpulse.ui.theme.Teal
import com.example.taskpulse.ui.theme.TealDim
import com.example.taskpulse.ui.theme.TextFaint
import com.example.taskpulse.ui.theme.TextHi
import com.example.taskpulse.ui.theme.TextLo
import kotlinx.coroutines.launch

enum class TimelineFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    RECURRING("Recurring"),
    DONE("Done")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onNavigateToAddTask: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    var currentFilter by remember { mutableStateOf(TimelineFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val filteredTasks = when (currentFilter) {
        TimelineFilter.ALL -> tasks
        TimelineFilter.TODAY -> tasks.filter { !it.isCompleted && !it.isRecurring }
        TimelineFilter.RECURRING -> tasks.filter { it.isRecurring }
        TimelineFilter.DONE -> tasks.filter { it.isCompleted }
    }

    val activeCount = tasks.count { !it.isCompleted }
    val urgentCount = tasks.count { !it.isCompleted && !it.isRecurring && it.dueMinutes <= 5 }

    Scaffold(
        containerColor = TaskPulseTheme.colors.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = PanelRaised,
                    contentColor = TextHi,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .border(1.dp, Line, RoundedCornerShape(12.dp))
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (onNavigateToAddTask != null) {
                        onNavigateToAddTask()
                    } else {
                        taskToEdit = null
                        showAddDialog = true
                    }
                },
                containerColor = Pulse,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                modifier = Modifier
                    .size(56.dp)
                    .border(1.dp, PulseDim, RoundedCornerShape(18.dp))
            ) {
                Text(
                    text = "+",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 16.dp, start = 20.dp, end = 20.dp)
        ) {
            // Header: Eyebrow + Title + Avatar
            TimelineHeader(
                activeCount = activeCount,
                urgentCount = urgentCount
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Filter Pills Row
            TimelineFilterRow(
                currentFilter = currentFilter,
                onFilterSelected = { currentFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Timeline Content: Loading, Empty, or Tasks List
            when (uiState) {
                is TaskListUiState.Loading -> {
                    TimelineLoadingState()
                }
                is TaskListUiState.Success -> {
                    if (tasks.isEmpty()) {
                        TimelineGlobalEmptyState(
                            onAddTask = {
                                if (onNavigateToAddTask != null) {
                                    onNavigateToAddTask()
                                } else {
                                    taskToEdit = null
                                    showAddDialog = true
                                }
                            }
                        )
                    } else if (filteredTasks.isEmpty()) {
                        TimelineFilteredEmptyState(
                            currentFilter = currentFilter,
                            onClearFilter = { currentFilter = TimelineFilter.ALL }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            itemsIndexed(filteredTasks, key = { _, task -> task.id }) { index, task ->
                                val isFirst = index == 0
                                val isLast = index == filteredTasks.lastIndex
                                val isUrgent = !task.isCompleted && !task.isRecurring && task.dueMinutes <= 5

                                TimelineTaskSwipeNode(
                                    task = task,
                                    isFirst = isFirst,
                                    isLast = isLast,
                                    isUrgent = isUrgent,
                                    onToggleComplete = { viewModel.toggleTaskCompleted(task) },
                                    onEdit = {
                                        taskToEdit = task
                                        showAddDialog = true
                                    },
                                    onDelete = {
                                        viewModel.deleteTask(task)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Deleted \"${task.title}\"")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog || taskToEdit != null) {
            AddTaskBottomSheet(
                viewModel = viewModel,
                onDismissRequest = {
                    showAddDialog = false
                    taskToEdit = null
                },
                taskToEdit = taskToEdit,
                onShowSnackbar = { message ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            )
        }
    }
}

@Composable
private fun TimelineHeader(
    activeCount: Int,
    urgentCount: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eyebrow_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )

    Column {
        // Eyebrow
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Pulse.copy(alpha = dotAlpha))
            )
            Text(
                text = "TaskPulse — timeline",
                style = MaterialTheme.typography.labelSmall,
                color = TextLo,
                letterSpacing = 0.5.sp
            )
        }

        // Title and Avatar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextHi
                )
                Spacer(modifier = Modifier.height(3.dp))
                val subtitle = buildString {
                    append("$activeCount reminders")
                    if (urgentCount > 0) {
                        append(", $urgentCount due now")
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextLo
                )
            }

            // Avatar circle button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PanelRaised)
                    .border(1.dp, Line, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TP",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextLo
                )
            }
        }
    }
}

@Composable
private fun TimelineFilterRow(
    currentFilter: TimelineFilter,
    onFilterSelected: (TimelineFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimelineFilter.values().size) { index ->
            val filter = TimelineFilter.values()[index]
            val isSelected = filter == currentFilter

            Surface(
                shape = RoundedCornerShape(100.dp),
                color = if (isSelected) Pulse else Color.Transparent,
                border = if (isSelected) null else BorderStroke(1.dp, Line),
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onFilterSelected(filter) }
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) Color.White else TextLo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineTaskSwipeNode(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    isUrgent: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isDismissed by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState()

    androidx.compose.runtime.LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            isDismissed = true
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = !isDismissed,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp)
        ) {
            // Vertical Timeline Column with Connecting Line + Status Dot
            TimelineDotTrack(
                task = task,
                isFirst = isFirst,
                isLast = isLast,
                isUrgent = isUrgent,
                onToggleComplete = onToggleComplete
            )

            Spacer(modifier = Modifier.width(12.dp))

            // SwipeToDismissBox wrapping the Task Card
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = false,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEDE7DE))
                            .border(1.dp, Color(0xFFD4CDC0), RoundedCornerShape(14.dp))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "✕ Delete",
                            color = Color(0xFFA64436),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            ) {
                TimelineTaskCard(
                    task = task,
                    isUrgent = isUrgent,
                    onToggleComplete = onToggleComplete,
                    onEdit = onEdit,
                    onDelete = onDelete
                )
            }
        }
    }
}

@Composable
private fun TimelineDotTrack(
    task: Task,
    isFirst: Boolean,
    isLast: Boolean,
    isUrgent: Boolean,
    onToggleComplete: () -> Unit
) {
    val trackLineColor = Line
    val trackPulseColor = Pulse

    // Pulsing halo animation for urgent/active tasks
    val infiniteTransition = rememberInfiniteTransition(label = "dot_beat")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_scale"
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_alpha"
    )

    Box(
        modifier = Modifier
            .width(36.dp)
            .fillMaxHeight()
            .drawBehind {
                val centerX = size.width / 2f
                val dotCenterY = 22.dp.toPx()

                // Top line segment
                if (!isFirst) {
                    drawLine(
                        color = trackLineColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, dotCenterY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Bottom line segment
                if (!isLast) {
                    drawLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                if (isUrgent) trackPulseColor else trackLineColor,
                                trackLineColor
                            ),
                            startY = dotCenterY,
                            endY = size.height
                        ),
                        start = Offset(centerX, dotCenterY),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            },
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(13.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated halo ring for urgent active tasks
            if (isUrgent && !task.isCompleted) {
                Box(
                    modifier = Modifier
                        .size(13.dp * haloScale)
                        .clip(CircleShape)
                        .background(Pulse.copy(alpha = haloAlpha))
                )
            }

            // Status Dot
            val dotColor = when {
                task.isCompleted -> Teal
                task.isRecurring -> Amber
                else -> Pulse
            }

            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) Teal else Fog)
                    .border(2.dp, dotColor, CircleShape)
                    .clickable { onToggleComplete() }
            )
        }
    }
}

@Composable
private fun TimelineTaskCard(
    task: Task,
    isUrgent: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBackground = if (isUrgent && !task.isCompleted) {
        Brush.verticalGradient(listOf(Color(0xFFE6EAF2), Paper))
    } else {
        Brush.verticalGradient(listOf(Paper, Paper))
    }

    val cardBorder = if (isUrgent && !task.isCompleted) {
        BorderStroke(1.dp, Pulse)
    } else {
        BorderStroke(1.dp, Line)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        border = cardBorder,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBackground)
            .clickable { onEdit() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Top Row: Title + Time label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) TextFaint else TextHi,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Time label
                val timeText = when {
                    task.isCompleted -> "Done"
                    task.isRecurring -> "Daily"
                    task.dueMinutes <= 1 -> "1m"
                    else -> "${task.dueMinutes}m"
                }

                val timeColor = when {
                    task.isCompleted -> Teal
                    task.isRecurring -> Amber
                    isUrgent -> Pulse
                    else -> Pulse
                }

                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = timeColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meta Row: Tags & Action buttons (Edit + Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        task.isCompleted -> {
                            TimelineTag(text = "✓ Completed", textColor = Teal, bgColor = TealDim)
                        }
                        task.isRecurring -> {
                            TimelineTag(text = "↻ Daily", textColor = Amber, bgColor = AmberDim)
                        }
                        isUrgent -> {
                            TimelineTag(text = "Due now", textColor = Pulse, bgColor = PulseDim)
                        }
                        else -> {
                            TimelineTag(text = "Once", textColor = TextLo, bgColor = PanelRaised)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = "✎",
                            color = TextLo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text(
                            text = "✕",
                            color = TextFaint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineTag(
    text: String,
    textColor: Color,
    bgColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
private fun TimelineLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Pulse,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "Aligning pulse timeline...",
                style = MaterialTheme.typography.bodyMedium,
                color = TextLo
            )
        }
    }
}

@Composable
private fun TimelineGlobalEmptyState(onAddTask: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsing beat-dot icon
            Box(
                modifier = Modifier
                    .size(48.dp * pulseScale)
                    .clip(CircleShape)
                    .background(PulseDim)
                    .border(2.dp, Pulse, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Pulse)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "No tasks on your pulse",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextHi,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Create your first reminder to start your timeline trace.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextLo,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddTask,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Pulse,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "+ Add Reminder",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TimelineFilteredEmptyState(
    currentFilter: TimelineFilter,
    onClearFilter: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = when (currentFilter) {
                    TimelineFilter.ALL -> "No reminders found"
                    TimelineFilter.TODAY -> "No active reminders today"
                    TimelineFilter.RECURRING -> "No recurring reminders"
                    TimelineFilter.DONE -> "No completed reminders"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextHi
            )
            Text(
                text = "You're all caught up for this view.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextLo
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PanelRaised,
                border = BorderStroke(1.dp, Line),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onClearFilter() }
            ) {
                Text(
                    text = "View All Reminders",
                    style = MaterialTheme.typography.labelSmall,
                    color = Pulse,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
