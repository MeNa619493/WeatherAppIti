package com.example.weatherapp.workmanager

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.data.repos.Repo
import com.example.weatherapp.utils.Utils
import com.example.weatherapp.utils.Utils.getDateMillis
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyWorkManager @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repo: Repo,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.e("doWork", "periodicWorkRequest")
        getTodayAlerts()
        return Result.success()
    }

    private suspend fun getTodayAlerts() {
        val alerts = repo.getAllAerts(System.currentTimeMillis()).first()
        alerts.forEach { alert ->
            scheduleTodayAlerts(alert)
        }
    }

    private fun scheduleTodayAlerts(alert: WeatherAlert) {
        if (isAlertToday(alert)) {
            val delay: Long = getDifferenceTimeStamp(alert)
            setHourlyWorkManger(
                delay,
                alert.id?:0
            )
        }
    }

    private fun isAlertToday(alert: WeatherAlert): Boolean {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val date = "$day/${month + 1}/$year"
        val currentDay = getDateMillis(date)
        return currentDay >= alert.startDate && currentDay <= alert.endDate
    }

    private fun getDifferenceTimeStamp(alert: WeatherAlert): Long {
        val hour =
            TimeUnit.HOURS.toMillis(Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toLong())
        val minute =
            TimeUnit.MINUTES.toMillis(Calendar.getInstance().get(Calendar.MINUTE).toLong())
        return alert.timeFrom - (hour + minute)
    }

    private fun setHourlyWorkManger(delay: Long, id: Int) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(HourlyWorkManger::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "$id",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )
        Log.e("setHourlyWorkManger", "oneTimeWorkRequest done")
    }
}