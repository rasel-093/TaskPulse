package com.example.taskpulse.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun testReminderWorker_initialDelayMet_completesSuccessfully() {
        val taskId = 100L
        val taskTitle = "Test Reminder"

        val inputData = Data.Builder()
            .putLong(ReminderWorker.TASK_ID_KEY, taskId)
            .putString(ReminderWorker.TASK_TITLE_KEY, taskTitle)
            .build()

        // Create work request with a 30-minute delay
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(30, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        // Initially in ENQUEUED state waiting for delay
        var workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo?.state, `is`(WorkInfo.State.ENQUEUED))

        // Fast-forward delay using WorkManagerTestInitHelper without waiting on real time
        testDriver.setInitialDelayMet(request.id)

        // Work should run and succeed
        workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo?.state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun testReminderWorker_withConstraints_completesWhenConstraintsAndDelayMet() {
        val taskId = 101L
        val taskTitle = "Constrained Task"

        val inputData = Data.Builder()
            .putLong(ReminderWorker.TASK_ID_KEY, taskId)
            .putString(ReminderWorker.TASK_TITLE_KEY, taskTitle)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(60, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!

        // Fast-forward delay and constraints without waiting
        testDriver.setInitialDelayMet(request.id)
        testDriver.setAllConstraintsMet(request.id)

        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo?.state, `is`(WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun testReminderWorker_missingTaskId_fails() {
        val inputData = Data.Builder()
            .putString(ReminderWorker.TASK_TITLE_KEY, "Missing Task Id")
            // Intentionally omit TASK_ID_KEY
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(10, TimeUnit.MINUTES)
            .build()

        val workManager = WorkManager.getInstance(context)
        workManager.enqueue(request).result.get()

        val testDriver = WorkManagerTestInitHelper.getTestDriver(context)!!
        testDriver.setInitialDelayMet(request.id)

        val workInfo = workManager.getWorkInfoById(request.id).get()
        assertThat(workInfo?.state, `is`(WorkInfo.State.FAILED))
    }
}
