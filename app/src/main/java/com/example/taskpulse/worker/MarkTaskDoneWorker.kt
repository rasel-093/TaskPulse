package com.example.taskpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.taskpulse.data.TaskDatabase

class MarkTaskDoneWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(TASK_ID_KEY, -1L)
        if (taskId == -1L) return Result.failure()

        val database = TaskDatabase.getDatabase(applicationContext)
        database.taskDao().updateTaskCompletion(taskId, true)

        // Ensure any remaining scheduled work for this task is cancelled
        WorkManager.getInstance(applicationContext).cancelAllWorkByTag(taskId.toString())

        return Result.success()
    }

    companion object {
        const val TASK_ID_KEY = "TASK_ID_KEY"
    }
}
