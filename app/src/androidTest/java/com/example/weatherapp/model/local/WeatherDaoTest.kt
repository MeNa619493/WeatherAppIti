package com.example.weatherapp.model.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import app.cash.turbine.test
import com.example.weatherapp.TestDispatcherRule
import com.example.weatherapp.model.pojo.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SmallTest
class WeatherDaoTest {

    @get: Rule
    val dispatcherRule = TestDispatcherRule()

    private lateinit var database: WeatherDatabase
    private lateinit var dao: WeatherDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WeatherDatabase::class.java
        ).allowMainThreadQueries()
            .build()
        dao = database.weatherDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertWeatherItem_shouldReturn_theItem_inFlow() = runTest  {
        val weather = WeatherResponse(
            1,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )
        dao.insertWeather(weather)

        dao.getAllWeather().test {
            val list = awaitItem()
            assert(list.contains(weather))
            cancel()
        }
    }

    @Test
    fun deletedWeatherItem_shouldNot_be_present_inFlow() = runTest {
        val weather1 = WeatherResponse(
            1,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )
        val weather2 = WeatherResponse(
            2,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )

        dao.insertWeather(weather1)
        dao.insertWeather(weather2)
        dao.deleteWeather(weather2)

        dao.getAllWeather().test  {
            val list = awaitItem()
            assert(list.size == 1)
            assert(list.contains(weather1))
            cancel()
        }
    }

    @Test
    fun updateItem_shouldReturn_theItem_inFlow() = runTest {
        val weather1 = WeatherResponse(
            1,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )
        val weather2 = WeatherResponse(
            2,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )

        dao.insertWeather(weather1)
        dao.insertWeather(weather2)

        dao.getAllWeather().test {
            val list = awaitItem()
            assert(list.size == 2)
            assert(list.contains(weather1))
            assert(list.contains(weather2))
            cancel()
        }
    }
}