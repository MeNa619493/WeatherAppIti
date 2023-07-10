package com.example.weatherapp.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.MainCoroutineRule
import com.example.weatherapp.getOrAwaitValue
import com.example.weatherapp.model.pojo.Alert
import com.example.weatherapp.model.pojo.Daily
import com.example.weatherapp.model.pojo.Hourly
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.repos.FakeRepo
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class SharedViewModelTest {
    // Subject under test
    private lateinit var viewModel: SharedViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var repository: FakeRepo

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private val weather1 = WeatherResponse(
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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = FakeRepo()
        repository.weatherData.add(weather1)
        viewModel = SharedViewModel(repository, mFusedLocationProviderClient)
    }

    @Test
    fun getCurrentWeather_shouldSuccess() {
        // When
        viewModel.getCurrentWeather("","","","")

        // Then
        val value = viewModel.weather.getOrAwaitValue()

        Assert.assertThat(value.data, not(nullValue()))
        Assert.assertEquals(value.data, weather1)
    }

    @Test
    fun getCurrentWeather_shouldFail() {
        //Given
        repository.setReturnError(true)

        // When
        viewModel.getCurrentWeather("","","","")

        // Then
        val value = viewModel.weather.getOrAwaitValue()

        Assert.assertThat(value.data, `is`(nullValue()))
    }

    @Test
    fun saveFavLocationWeather_shouldReturnSizeOfWeatherData() {
        // When
        viewModel.saveFavLocationWeather("","","","")

        // Then
        Assert.assertEquals(repository.weatherData.size, 2)
    }

    @Test
    fun addFavLocation_shouldReturnSizeOfWeatherData() {
        // When
        viewModel.addFavLocation(weather2)

        // Then
        Assert.assertEquals(repository.weatherData.size, 2)
    }

    @Test
    fun getAllFavs_returnAllWeathers() {
        // When
        val value = viewModel.getAllFavs().getOrAwaitValue()

        // Then
        Assert.assertThat(value, not(nullValue()))
        Assert.assertEquals(value[0], weather1)
    }

    @Test
    fun deleteAlert_returnId() {
        // When
        viewModel.addFavLocation(weather2)
        viewModel.deleteFav(weather2)

        // Then
        Assert.assertEquals(repository.weatherData.size, 1)
    }
}