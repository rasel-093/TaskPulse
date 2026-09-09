package com.example.taskpulse.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.Task
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    viewModel: TaskListViewModel,
    onTaskCreated: (taskId: Long) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var dueMinutesText by remember { mutableStateOf("5") }
    var isRecurring by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = viewModel.repository

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Task") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("← Back", fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (errorMessage != null) errorMessage = null
                },
                label = { Text("Task Title") },
                placeholder = { Text("e.g. Complete homework") },
                singleLine = true,
                isError = errorMessage != null,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = dueMinutesText,
                onValueChange = { dueMinutesText = it.filter { char -> char.isDigit() } },
                label = { Text("Reminder Delay (Minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = !isRecurring,
                supportingText = if (isRecurring) {
                    { Text("Daily reminder repeats every 24 hours") }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Repeat daily",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Task title cannot be empty"
                        return@Button
                    }
                    val dueMinutes = dueMinutesText.toLongOrNull() ?: 1L
                    if (!isRecurring && (dueMinutesText.isBlank() || dueMinutes <= 0L)) {
                        errorMessage = "Delay must be at least 1 minute"
                        return@Button
                    }

                    coroutineScope.launch {
                        val task = Task(
                            title = title.trim(),
                            dueMinutes = if (isRecurring) 0L else dueMinutes,
                            isRecurring = isRecurring
                        )
                        // Insert task into repository and retrieve generated taskId
                        val generatedTaskId = viewModel.insertTask(task)

                        if (isRecurring) {
                            // Schedule daily recurring reminder
                            viewModel.scheduleRecurringReminder(
                                taskId = generatedTaskId,
                                taskTitle = task.title
                            )
                            Toast.makeText(
                                context,
                                "Task added! Daily recurring reminder scheduled",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Schedule one-time reminder
                            viewModel.scheduleReminder(
                                taskId = generatedTaskId,
                                taskTitle = task.title,
                                delayMinutes = task.dueMinutes
                            )
                            Toast.makeText(
                                context,
                                "Task added! Reminder scheduled in $dueMinutes min",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        onTaskCreated(generatedTaskId)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRecurring) "Create Task & Schedule Daily Reminder" else "Create Task & Schedule Reminder",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    viewModel: TaskListViewModel,
    onDismissRequest: () -> Unit,
    onTaskCreated: (taskId: Long) -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var dueMinutesText by remember { mutableStateOf("5") }
    var isRecurring by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = viewModel.repository

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Task") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (errorMessage != null) errorMessage = null
                    },
                    label = { Text("Task Title") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = dueMinutesText,
                    onValueChange = { dueMinutesText = it.filter { char -> char.isDigit() } },
                    label = { Text("Reminder (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !isRecurring,
                    supportingText = if (isRecurring) {
                        { Text("Repeats daily every 24 hours") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Repeat daily")
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Title cannot be empty"
                        return@Button
                    }
                    val dueMinutes = dueMinutesText.toLongOrNull() ?: 1L
                    if (!isRecurring && (dueMinutesText.isBlank() || dueMinutes <= 0L)) {
                        errorMessage = "Delay must be at least 1 min"
                        return@Button
                    }

                    coroutineScope.launch {
                        val task = Task(
                            title = title.trim(),
                            dueMinutes = if (isRecurring) 0L else dueMinutes,
                            isRecurring = isRecurring
                        )
                        // After repository.insert(task) succeeds and we have generated taskId
                        val generatedTaskId = viewModel.insertTask(task)

                        if (isRecurring) {
                            // Schedule recurring reminder
                            viewModel.scheduleRecurringReminder(
                                taskId = generatedTaskId,
                                taskTitle = task.title
                            )
                            Toast.makeText(
                                context,
                                "Task added! Daily recurring reminder scheduled",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Schedule one-time reminder
                            viewModel.scheduleReminder(
                                taskId = generatedTaskId,
                                taskTitle = task.title,
                                delayMinutes = task.dueMinutes
                            )
                            Toast.makeText(
                                context,
                                "Task added! Reminder in $dueMinutes min",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        onTaskCreated(generatedTaskId)
                        onDismissRequest()
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}
