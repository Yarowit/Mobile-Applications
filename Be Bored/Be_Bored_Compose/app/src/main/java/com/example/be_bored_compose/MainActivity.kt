package com.example.be_bored_compose

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.Data
import androidx.compose.material.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.be_bored_compose.database.AppDatabase
import com.example.be_bored_compose.database.Database
import com.example.be_bored_compose.ui.theme.Be_Bored_ComposeTheme
import android.provider.Settings
import androidx.compose.material.*
import androidx.compose.ui.graphics.Color
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inicjalizacja bazy danych
        Database.db = Room.databaseBuilder(
            baseContext,
            AppDatabase::class.java, "database-name"
        ).allowMainThreadQueries().build()

//        Database.db.dao().nukeTable()
//        Database.db.dao().nukeGoals()

        //uruchomienie service
        Intent(this, BackgroundTracker::class.java).also { intent ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        setContent {
            Be_Bored_ComposeTheme(){
                val context = LocalContext.current
                var hasNotificationPermission by remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    } else mutableStateOf(true)
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val launcher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission(),
                        onResult = { isGranted ->
                            hasNotificationPermission = isGranted
                        }
                    )

                    /*Button(onClick = {

                    Button(onClick = {
                        requestAppUsagePermission()

                        if(hasNotificationPermission) {
                            showAlert("Test of notification", "Here you can have some message", R.drawable.sad_dog)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    }) {
                        Text(text = "Show notification")
                    }*/

                    Button(onClick = {
                        val intent = Intent(context, Statistics::class.java)
                        startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(113,91,164),
                        contentColor = Color(255,200,195)
                        )
                    ) {
                        Text(text = "Show statistics")
                    }

                    Button(onClick = {
                        val intent = Intent(context, GoalsActivity::class.java)
                        startActivity(intent)
                    },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(113,91,164),
                            contentColor = Color(255,200,195)
                        )
                    ) {
                        Text(text = "Show goals")
                    }
                }
            }
        }
    }

    private fun showAlert(title: String, message: String, image: Int) {
        val notification = NotificationCompat.Builder(applicationContext, "alerts")
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(message)
            .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources, image))
            .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }

    private fun requestAppUsagePermission() {
        val appUsagePermission = Manifest.permission.PACKAGE_USAGE_STATS
        val packageManager = packageManager
        val granted = packageManager.checkPermission(appUsagePermission, packageName) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }
}
