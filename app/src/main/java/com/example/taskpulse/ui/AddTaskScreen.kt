package com.example.taskpulse.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import com.example.taskpulse.data.Task
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
import com.example.taskpulse.ui.theme.TextFaint
import com.example.taskpulse.ui.theme.TextHi
import com.example.taskpulse.ui.theme.TextLo
import kotlinx.coroutines.launch

enum class QuickTimePreset(val label: String, val minutes: Long) {
    TEN_MIN("10 min", 10L),
    ONE_HOUR("1 hour", 60L),
    TOMORROW("Tomorrow", 1440L),
    CUSTOM("Custom", 0L)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    viewModel: TaskListViewModel,
    onDismissRequest: () -> Unit,
    taskToEdit: Task? = null,
    onTaskCreated: (taskId: Long) -> Unit = {},
    onShowSnackbar: ((String) -> Unit)? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Paper,
        scrimColor = Color(0x662B2924),
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 18.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .background(Line, RoundedCornerShape(4.dp))
            )
        }
    ) {
        AddTaskSheetContent(
            viewModel = viewModel,
            onDismissRequest = onDismissRequest,
            taskToEdit = taskToEdit,
            onTaskCreated = onTaskCreated,
            onShowSnackbar = onShowSnackbar
        )
    }
}

@Composable
fun AddTaskSheetContent(
    viewModel: TaskListViewModel,
    onDismissRequest: () -> Unit,
    taskToEdit: Task? = null,
    onTaskCreated: (taskId: Long) -> Unit = {},
    onShowSnackbar: ((String) -> Unit)? = null
) {
    val isEditMode = taskToEdit != null
    var title by remember(taskToEdit) { mutableStateOf(taskToEdit?.title ?: "") }

    val initialPreset = remember(taskToEdit) {
        if (taskToEdit == null) {
            QuickTimePreset.ONE_HOUR
        } else {
            when (taskToEdit.dueMinutes) {
                10L -> QuickTimePreset.TEN_MIN
                60L -> QuickTimePreset.ONE_HOUR
                1440L -> QuickTimePreset.TOMORROW
                else -> QuickTimePreset.CUSTOM
            }
        }
    }
    var selectedPreset by remember(taskToEdit) { mutableStateOf(initialPreset) }
    var customMinutesText by remember(taskToEdit) {
        mutableStateOf(
            if (taskToEdit != null && taskToEdit.dueMinutes !in listOf(10L, 60L, 1440L) && taskToEdit.dueMinutes > 0L) {
                taskToEdit.dueMinutes.toString()
            } else {
                "15"
            }
        )
    }
    var isRecurring by remember(taskToEdit) { mutableStateOf(taskToEdit?.isRecurring ?: false) }
    var silentHours by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 26.dp)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Title matching design spec Space Grotesk 20/600
        Text(
            text = if (isEditMode) "Edit reminder" else "New reminder",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = TextHi,
            modifier = Modifier.padding(bottom = 22.dp)
        )

        // Field Group: What do you need to do?
        Text(
            text = "What do you need to do?",
            style = MaterialTheme.typography.bodySmall,
            color = TextLo,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(PaperShade)
                .border(
                    width = 1.dp,
                    color = if (errorMessage != null) MaterialTheme.colorScheme.error else Line,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 14.dp, vertical = 13.dp)
        ) {
            if (title.isEmpty()) {
                Text(
                    text = "e.g. Water the pothos",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextFaint,
                    fontSize = 14.5.sp
                )
            }
            BasicTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (errorMessage != null) errorMessage = null
                },
                textStyle = TextStyle(
                    color = TextHi,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Normal
                ),
                cursorBrush = SolidColor(Pulse),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Field Group: Remind me in (Time Pills)
        Text(
            text = "Remind me in",
            style = MaterialTheme.typography.bodySmall,
            color = TextLo,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickTimePreset.values().forEach { preset ->
                val isSelected = selectedPreset == preset
                val pillBorder = if (isSelected) Pulse else Line
                val pillBg = if (isSelected) PulseDim else PaperShade
                val pillTextColor = if (isSelected) Pulse else TextLo

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(pillBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedPreset = preset }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = preset.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = pillTextColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Custom minutes input if Custom preset selected
        if (selectedPreset == QuickTimePreset.CUSTOM) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PaperShade)
                    .border(1.dp, Line, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = customMinutesText,
                        onValueChange = { customMinutesText = it.filter { char -> char.isDigit() } },
                        textStyle = TextStyle(
                            color = TextHi,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(Pulse),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "minutes",
                        color = TextLo,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Toggle Row 1: Repeat daily
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Transparent) // spacer
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Repeat daily",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp,
                    color = TextHi
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Fires again ~24h after each reminder",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = TextLo
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = isRecurring,
                onCheckedChange = { isRecurring = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Pulse,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Line,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        // Toggle Row 2: Silent hours
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Silent hours",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.5.sp,
                    color = TextHi
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Skip notifications 11PM–7AM",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = TextLo
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = silentHours,
                onCheckedChange = { silentHours = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Pulse,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Line,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Save Button: Set reminder
        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "Task title cannot be empty"
                    return@Button
                }

                val finalMinutes: Long = when (selectedPreset) {
                    QuickTimePreset.TEN_MIN -> 10L
                    QuickTimePreset.ONE_HOUR -> 60L
                    QuickTimePreset.TOMORROW -> 1440L
                    QuickTimePreset.CUSTOM -> {
                        val parsed = customMinutesText.toLongOrNull() ?: 15L
                        if (parsed <= 0L) 1L else parsed
                    }
                }

                coroutineScope.launch {
                    if (taskToEdit != null) {
                        // 3a. Call repository.updateTask() with the new values
                        val updatedTask = taskToEdit.copy(
                            title = title.trim(),
                            dueMinutes = if (isRecurring) 0L else finalMinutes,
                            isRecurring = isRecurring
                        )
                        viewModel.updateTask(updatedTask)

                        // 3b, 3c, 3d. Reschedule the reminder work
                        val wasRecurring = taskToEdit.isRecurring
                        if (isRecurring) {
                            if (!wasRecurring) {
                                // 3d. If isRecurring was toggled ON during edit (was one-time, now recurring),
                                // cancel any pending one-time work for this taskId first, then call scheduleRecurringReminder
                                viewModel.cancelReminder(taskToEdit.id)
                            }
                            // For recurring reminders, UPDATE remains correct since it already handles in-place edits
                            viewModel.repository.scheduleRecurringReminder(
                                taskId = taskToEdit.id,
                                taskTitle = updatedTask.title,
                                policy = ExistingPeriodicWorkPolicy.UPDATE
                            )
                        } else {
                            if (wasRecurring) {
                                // 3c. If isRecurring was toggled OFF during edit, call cancelReminder(taskId)
                                // to stop the periodic work, since enqueueing a new one-time request won't
                                // automatically cancel an existing periodic one under the same tag
                                viewModel.cancelReminder(taskToEdit.id)
                            }
                            // 3b. Use ExistingWorkPolicy.REPLACE (not KEEP) for one-time reminders in the edit path
                            viewModel.repository.scheduleReminder(
                                taskId = taskToEdit.id,
                                taskTitle = updatedTask.title,
                                delayMinutes = updatedTask.dueMinutes,
                                policy = ExistingWorkPolicy.REPLACE
                            )
                        }

                        // 5. Show confirmation: "Reminder updated — [time]"
                        val confirmationMessage = if (isRecurring) {
                            "Reminder updated — repeats daily"
                        } else {
                            val targetTime = java.time.LocalTime.now().plusMinutes(updatedTask.dueMinutes)
                            val timeFormatted = targetTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
                            "Reminder updated — $timeFormatted"
                        }

                        if (onShowSnackbar != null) {
                            onShowSnackbar(confirmationMessage)
                        } else {
                            Toast.makeText(context, confirmationMessage, Toast.LENGTH_SHORT).show()
                        }

                        onTaskCreated(taskToEdit.id)
                        onDismissRequest()
                    } else {
                        // Create mode
                        val task = Task(
                            title = title.trim(),
                            dueMinutes = if (isRecurring) 0L else finalMinutes,
                            isRecurring = isRecurring
                        )
                        val generatedTaskId = viewModel.insertTask(task)

                        if (isRecurring) {
                            viewModel.scheduleRecurringReminder(
                                taskId = generatedTaskId,
                                taskTitle = task.title
                            )
                        } else {
                            viewModel.scheduleReminder(
                                taskId = generatedTaskId,
                                taskTitle = task.title,
                                delayMinutes = task.dueMinutes
                            )
                        }

                        val confirmationMessage = if (isRecurring) {
                            "Reminder set for ${task.title} — repeats daily"
                        } else {
                            val targetTime = java.time.LocalTime.now().plusMinutes(task.dueMinutes)
                            val timeFormatted = targetTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))
                            "Reminder set for ${task.title} — $timeFormatted"
                        }

                        if (onShowSnackbar != null) {
                            onShowSnackbar(confirmationMessage)
                        } else {
                            Toast.makeText(context, confirmationMessage, Toast.LENGTH_SHORT).show()
                        }

                        onTaskCreated(generatedTaskId)
                        onDismissRequest()
                    }
                }
            },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Pulse,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = if (isEditMode) "Save changes" else "Set reminder",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}

/**
 * Backward compatibility functions so existing call sites continue working
 */
@Composable
fun AddTaskScreen(
    viewModel: TaskListViewModel,
    taskToEdit: Task? = null,
    onTaskCreated: (taskId: Long) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Fog)
            .padding(top = 24.dp)
    ) {
        AddTaskSheetContent(
            viewModel = viewModel,
            onDismissRequest = onNavigateBack,
            taskToEdit = taskToEdit,
            onTaskCreated = onTaskCreated
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    viewModel: TaskListViewModel,
    onDismissRequest: () -> Unit,
    taskToEdit: Task? = null,
    onTaskCreated: (taskId: Long) -> Unit = {}
) {
    AddTaskBottomSheet(
        viewModel = viewModel,
        onDismissRequest = onDismissRequest,
        taskToEdit = taskToEdit,
        onTaskCreated = onTaskCreated
    )
}
