package com.example.weatherapp.workmanager

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.repos.Repo
import com.example.weatherapp.utils.Constants
import com.example.weatherapp.utils.Constants.DESCRIPTION
import com.example.weatherapp.utils.Constants.ICON
import com.example.weatherapp.utils.Constants.getDateMillis
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.*
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@HiltWorker
class DailyWorkManager @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repo: Repo
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.e("doWork", "periodicWorkRequest")
        getTodayAlerts()
        return Result.success()
    }

    private suspend fun getTodayAlerts() {
        val weatherResponse = repo.getCurrentWeather(
            repo.getString(Constants.LAT, "0.0"),
            repo.getString(Constants.LONG, "0.0"),
            "en",
            "standard"
        )
        val currentWeather = weatherResponse.body()
        val alerts = repo.getAllAerts().first()
        alerts.forEach { alert ->
            if (isAlertToday(alert)) {
                val delay: Long = getDifferenceTimeStamp(alert)
                if (currentWeather?.alerts.isNullOrEmpty()) {
                    currentWeather?.current?.weather?.get(0)?.let {
                        setHourlyWorkManger(
                            delay,
                            alert.id,
                            it.description ?: "",
                            currentWeather.current.weather[0].icon ?: ""
                        )
                    }
                } else {
                    currentWeather?.alerts?.get(0)?.let {
                        setHourlyWorkManger(
                            delay,
                            alert.id,
                            it.description ?: "",
                            currentWeather.current?.weather?.get(0)?.icon ?: ""
                        )
                    }
                }
            }
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

    private fun setHourlyWorkManger(delay: Long, id: Int?, description: String, icon: String) {
        val data = Data.Builder()
        data.putString(DESCRIPTION, description)
        data.putString(ICON, icon)
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(HourlyWorkManger::class.java)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "$id",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )

        Log.e("setHourlyWorkManger", "oneTimeWorkRequest done")
    }


}