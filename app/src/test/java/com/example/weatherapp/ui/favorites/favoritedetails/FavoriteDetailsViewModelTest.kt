package com.example.weatherapp.ui.favorites.favoritedetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.MainCoroutineRule
import com.example.weatherapp.getOrAwaitValue
import com.example.weatherapp.model.pojo.Alert
import com.example.weatherapp.model.pojo.Daily
import com.example.weatherapp.model.pojo.Hourly
import com.example.weatherapp.model.pojo.WeatherResponse
import com.example.weatherapp.model.repos.FakeRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import com.example.weatherapp.utils.Event
import com.example.weatherapp.utils.NetworkResult
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FavoriteDetailsViewModelTest {
    // Subject under test
    private lateinit var viewModel: FavoriteDetailsViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var repository: FakeRepo

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val weather1 = WeatherResponse(
        1,
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
    fun setupViewModel() {
        repository = FakeRepo()
        repository.weatherData.add(weather1)
        viewModel = FavoriteDetailsViewModel(repository)
    }

    @Test
    fun getFavWeather_shouldSuccess() {
        // When
        viewModel.getFavWeather("","","","")

        // Then
        val value = viewModel.weather.getOrAwaitValue()

        assertThat(value.data, not(nullValue()))
        assertEquals(value.data, weather1)
    }

    @Test
    fun getFavWeather_shouldFail() {
        //Given
        repository.setReturnError(true)

        // When
        viewModel.getFavWeather("","","","")

        // Then
        val value = viewModel.weather.getOrAwaitValue()
        assertThat(value.data, `is`(nullValue()))
    }
}