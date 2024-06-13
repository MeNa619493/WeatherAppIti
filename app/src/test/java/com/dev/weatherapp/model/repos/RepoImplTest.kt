package com.dev.weatherapp.model.repos

import com.dev.weatherapp.MainCoroutineRule
import com.dev.weatherapp.model.data.local.AlertDao
import com.dev.weatherapp.model.data.local.WeatherDao
import com.dev.weatherapp.model.data.local.WeatherDatabase
import com.dev.weatherapp.model.pojo.*
import kotlinx.coroutines.flow.Flow
import com.dev.weatherapp.model.data.remote.ApiService
import com.dev.weatherapp.model.data.repos.Repo
import com.dev.weatherapp.model.data.repos.RepoImpl
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import org.mockito.Mockito.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.Response

@ExperimentalCoroutinesApi
class RepoImplTest {

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var apiService: ApiService

    @Mock
    private lateinit var weatherDao: WeatherDao

    @Mock
    private lateinit var alertDao: AlertDao

    private lateinit var repoImpl: Repo

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repoImpl = RepoImpl(apiService, weatherDao, alertDao)
    }

    @After
    fun cleanup() {
        Dispatchers.resetMain()

        // Clear all mock interactions
        unmockkAll()
    }

    @Test
    fun `getCurrentWeather should return success state when API response is successful`() =
        runBlocking {
            // Mock the API response
            val weatherResponse = WeatherResponse(
                1,
                false,
                null,
                listOf<Daily>(),
                listOf<Hourly>(),
                null,
                null,
                null,
                null,
                listOf<Alert>()
            )

            `when`(
                apiService.getCurrentWeather(
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString(),
                    anyString()
                )
            ).thenReturn(Response.success(weatherResponse))

            // Invoke the function under test
            val result = repoImpl.getCurrentWeather("30.04", "31.23", "metric", "ar")

            assertEquals(result.body(), weatherResponse)
        }

    @Test
    fun `getCurrentWeather with no parameters should return cached weather`() =
        runBlocking {
            // Mock the API response
            val weatherResponse = WeatherResponse(
                1,
                false,
                null,
                listOf<Daily>(),
                listOf<Hourly>(),
                null,
                null,
                null,
                null,
                listOf<Alert>()
            )

            `when`(weatherDao.getCurrentWeather()).thenReturn(weatherResponse)

            // Invoke the function under test
            val result = repoImpl.getCurrentWeather()

            assertEquals(result, weatherResponse)
            assertEquals(result.isFavourite, false)
        }


    @Test
    fun `insertWeather should success when call insert in weather dao`() = runBlocking {
        val weatherResponse = WeatherResponse(
            1,
            false,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )

        // Invoke the function under test
        repoImpl.insertWeather(weatherResponse)

        // Verify that the insertWeather method was called with the correct data
        verify(weatherDao).insertWeather(weatherResponse)
    }

    @Test
    fun `getAllWeather should success when return flow of weathers`() =
        runBlocking {

            val weatherResponse = WeatherResponse(
                1,
                false,
                null,
                listOf<Daily>(),
                listOf<Hourly>(),
                null,
                null,
                null,
                null,
                listOf<Alert>()
            )

            // Mock the DAO
            `when`(weatherDao.getAllWeather()).thenReturn(flowOf(listOf(weatherResponse)))

            // Invoke the function under test
            val result = repoImpl.getAllWeather()

            // Collect the flow and verify the emitted objects
            val items = mutableListOf<List<WeatherResponse>>()
            result.collect { items.add(it) }
            assertEquals(weatherResponse, items[0].first())
        }

    @Test
    fun `deleteWeather should success when call delete weather in weather dao`() = runBlocking {
        val weatherResponse = WeatherResponse(
            1,
            false,
            null,
            listOf<Daily>(),
            listOf<Hourly>(),
            null,
            null,
            null,
            null,
            listOf<Alert>()
        )

        // Invoke the function under test
        repoImpl.deleteWeather(weatherResponse)

        // Verify that the deleteWeather method was called with the correct data
        verify(weatherDao).deleteWeather(weatherResponse)
    }

    @Test
    fun `deleteCurrentWeather with no parameters should success when call delete in weather dao`() = runBlocking {
        // Invoke the function under test
        repoImpl.deleteCurrentWeather()

        // Verify that the deleteCurrentWeather method was called with the correct data
        verify(weatherDao).deleteCurrentWeather()
    }

    @Test
    fun `insertAlert should success when call insert alert in alert dao`() = runBlocking {
        val alert = WeatherAlert(
            1,
            0.0.toLong(),
            0.0.toLong(),
            0.0.toLong(),
            0.0.toLong()
        )

        // Mock the DAO
        `when`(alertDao.insertAlert(alert)).thenReturn(alert.id?.toLong())

        // Invoke the function under test
        val result = repoImpl.insertAlert(alert)

        assertEquals(alert.id, result.toInt())
    }

    @Test
    fun `getAllAlerts with timestamp parameter should success when return flow of alerts`() =
        runBlocking {

            val alert = WeatherAlert(
                1,
                0.0.toLong(),
                0.0.toLong() ,
                0.0.toLong(),
                0.0.toLong()
            )

            // Mock the DAO
            `when`(alertDao.getAllAerts(0.0.toLong())).thenReturn(flowOf(listOf(alert)))

            // Invoke the function under test
            val result = repoImpl.getAllAerts(0.0.toLong())

            // Collect the flow and verify the emitted objects
            val items = mutableListOf<List<WeatherAlert>>()
            result.collect { items.add(it) }
            assertEquals(alert, items[0].first())
        }

    @Test
    fun `deleteAlerts with timestamp parameter should success when call delete alert in alert dao`() = runBlocking {
        // Invoke the function under test
        repoImpl.deleteAlerts(0.0.toLong())

        // Verify that the deleteAlerts method was called with the correct data
        verify(alertDao).deleteAlerts(0.0.toLong())
    }

    @Test
    fun `deleteAlert should success when call delete in alert dao`() = runBlocking {
        val alert = WeatherAlert(
            1,
            0.0.toLong(),
            0.0.toLong(),
            0.0.toLong(),
            0.0.toLong()
        )

        // Mock the DAO
        `when`(alertDao.deleteAlert(alert)).thenReturn(alert.id)

        // Invoke the function under test
        val result = repoImpl.deleteAlert(alert)

        assertEquals(alert.id, result)
    }
}