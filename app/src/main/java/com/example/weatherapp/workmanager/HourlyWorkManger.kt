package com.example.weatherapp.workmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.ui.MainActivity
import com.example.weatherapp.R
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.data.repos.Repo
import com.example.weatherapp.utils.AlertWindowOverlay
import com.example.weatherapp.utils.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*

@HiltWorker
class HourlyWorkManger @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repo: Repo,
    private val sharedPreferences: HelperSharedPreferences
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt(Utils.ALERT_ID, 0)
        withContext(Dispatchers.IO){
            startAlert(id)
        }
        Log.e("doWork", "oneTimeWorkRequest")
        return Result.success()
    }

    private suspend fun startAlert(id: Int) {
        val weatherResponse = repo.getCurrentWeather(
            sharedPreferences.getString(Utils.LAT, "0.0"),
            sharedPreferences.getString(Utils.LONG, "0.0"),
            sharedPreferences.getString(Utils.UNIT, "metric"),
            sharedPreferences.getString(Utils.LANGUAGE, "en")
        )
        val currentWeather = weatherResponse.body()

        if (currentWeather?.alerts.isNullOrEmpty()) {
            currentWeather?.current?.weather?.get(0)?.let {
                sendNotification(it.description?:"")
                startWindowAlert(it.description?:"", it.icon?:"")
            }
        } else {
            currentWeather?.alerts?.get(0)?.let {
                sendNotification(it.description?:"")
                startWindowAlert(it.description?:"", "01d")
            }
        }
    }

    private fun sendNotification(description: String) {
        val notification_id = 0

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val notification =
            NotificationCompat.Builder(applicationContext, Utils.NOTIFICATION_CHANNEL)
                .setContentTitle("Weather Status")
                .setContentText(description)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notification.setChannelId(Utils.NOTIFICATION_CHANNEL)

        val ringtone =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.antonio_vivaldi_storm)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            Utils.NOTIFICATION_CHANNEL,
            Utils.NOTIFICATION_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )

        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        channel.setSound(ringtone, audioAttributes)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(notification_id, notification.build())
    }

    private suspend fun startWindowAlert(description: String, icon: String) {
        if (Settings.canDrawOverlays(context)) {
            withContext(Dispatchers.Main) {
                val alertWindowManger = AlertWindowOverlay(context, description, icon)
                alertWindowManger.setAlertWindowManger()
            }
        }
    }
}
