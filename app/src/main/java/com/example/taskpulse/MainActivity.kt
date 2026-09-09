package com.example.taskpulse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.taskpulse.data.TaskDatabase
import com.example.taskpulse.data.TaskRepository
import com.example.taskpulse.ui.AddTaskScreen
import com.example.taskpulse.ui.TaskListScreen
import com.example.taskpulse.ui.TaskListViewModel
import com.example.taskpulse.ui.theme.TaskPulseTheme

class MainActivity : ComponentActivity() {

    private val repository by lazy {
        val database = TaskDatabase.getDatabase(applicationContext)
        TaskRepository(database.taskDao(), applicationContext)
    }

    private val viewModel: TaskListViewModel by viewModels {
        TaskListViewModel.Factory(repository)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders may not show notifications.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermission()

        setContent {
            TaskPulseTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by remember { mutableStateOf("taskList") }

                    if (currentScreen == "addTask") {
                        AddTaskScreen(
                            viewModel = viewModel,
                            onTaskCreated = {
                                currentScreen = "taskList"
                            },
                            onNavigateBack = {
                                currentScreen = "taskList"
                            }
                        )
                    } else {
                        TaskListScreen(
                            viewModel = viewModel,
                            onNavigateToAddTask = {
                                currentScreen = "addTask"
                            }
                        )
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}