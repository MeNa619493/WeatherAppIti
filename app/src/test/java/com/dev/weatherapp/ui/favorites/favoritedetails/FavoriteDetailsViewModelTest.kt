package com.dev.weatherapp.ui.favorites.favoritedetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dev.weatherapp.MainCoroutineRule
import com.dev.weatherapp.getOrAwaitValue
import com.dev.weatherapp.model.pojo.Alert
import com.dev.weatherapp.model.pojo.Daily
import com.dev.weatherapp.model.pojo.Hourly
import com.dev.weatherapp.model.pojo.WeatherResponse
import com.dev.weatherapp.model.repos.FakeRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import com.dev.weatherapp.utils.Event
import com.dev.weatherapp.utils.NetworkResult
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