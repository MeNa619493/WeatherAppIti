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
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import org.hamcrest.CoreMatchers.*
import org.junit.*
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

    private val weather2 = WeatherResponse(
        2,
        true,
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

    @After
    fun cleanup() {
        // Clear all mock interactions
        unmockkAll()
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
    fun getCurrentWeather_shouldFail_returnCashedWeather() {
        //Given
        repository.setReturnError(true)

        // When
        viewModel.getCurrentWeather("","","","")

        // Then
        val value = viewModel.weather.getOrAwaitValue()
        Assert.assertThat(value.data?.isFavourite, `is`(false))
    }

    @Test
    fun saveFavLocationWeather_shouldReturnSizeOfFavouriteWeatherData() {
        // When
        viewModel.saveFavLocationWeather("","","","")

        // Then
        val result = viewModel.getAllFavs().getOrAwaitValue()
        Assert.assertEquals(result.size, 2)
    }

    @Test
    fun addFavLocation_shouldReturnSizeOfFavouriteWeatherData() {
        // When
        viewModel.addFavLocation(weather2)

        // Then
        val result = repository.weatherData.count { it.isFavourite }
        Assert.assertEquals(result, 1)
    }

    @Test
    fun getAllFavs_returnAllFavouriteWeathers() {
        //Given
        repository.weatherData.add(weather2)

        // When
        val value = viewModel.getAllFavs().getOrAwaitValue()

        // Then
        Assert.assertThat(value, not(nullValue()))
        Assert.assertEquals(value[0], weather2)
    }

    @Test
    fun deleteFav_shouldReturnSizeOfWeatherData() {
        //Given
        repository.weatherData.add(weather2)

        // When
        viewModel.deleteFav(weather2)

        // Then
        Assert.assertEquals(repository.weatherData.size, 1)
    }
}