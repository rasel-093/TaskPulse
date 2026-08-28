package com.example.taskpulse

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.taskpulse.notification.sendNotification
import com.example.taskpulse.ui.theme.TaskPulseTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {

            TaskPulseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,     // Center items vertically
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            checkNotificationPermissionAndSend(this@MainActivity, "Test notification")
                        }) {
                            Text("Click to send notification")
                        }
                    }
                }
            }
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Permission Granted, Notification will be sent from next time", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission rejected", Toast.LENGTH_LONG).show()
            }
        }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun checkNotificationPermissionAndSend(context: Context, message: String){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
            sendNotification(message, context)
        }else{
            val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permission == PackageManager.PERMISSION_GRANTED){
                sendNotification(message, context)
            }else{
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}