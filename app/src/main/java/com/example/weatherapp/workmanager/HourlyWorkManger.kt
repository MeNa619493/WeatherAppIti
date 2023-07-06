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
import com.example.weatherapp.utils.AlertWindowOverlay
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.DESCRIPTION
import com.example.weatherapp.utils.Constants.ICON
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@HiltWorker
class HourlyWorkManger @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val description = inputData.getString(DESCRIPTION)!!
        val icon = inputData.getString(ICON)!!

        sendNotification(description)
        Log.e("doWork", "oneTimeWorkRequest")
        if (Settings.canDrawOverlays(context)) {
            GlobalScope.launch(Dispatchers.Main) {
                val alertWindowManger = AlertWindowOverlay(context, description, icon)
                alertWindowManger.setAlertWindowManger()
            }
        }
        return Result.success()
    }

    private fun sendNotification(description: String) {
        val notification_id = 0

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        val notification =
            NotificationCompat.Builder(applicationContext, Constants.NOTIFICATION_CHANNEL)
                .setContentTitle("Weather Status")
                .setContentText(description)
                .setSmallIcon(R.drawable.cloudy_sunny)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notification.setChannelId(Constants.NOTIFICATION_CHANNEL)

        val ringtone =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.packageName + "/" + R.raw.antonio_vivaldi_storm)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL,
            Constants.NOTIFICATION_NAME,
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
}