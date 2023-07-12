package com.example.weatherapp.ui.alerts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.weatherapp.MainCoroutineRule
import com.example.weatherapp.getOrAwaitValue
import com.example.weatherapp.model.pojo.WeatherAlert
import com.example.weatherapp.model.repos.FakeRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedAlertViewModelTest {
    // Subject under test
    private lateinit var viewModel: SharedAlertViewModel

    // Use a fake repository to be injected into the viewmodel
    private lateinit var repository: FakeRepo

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val alert1 = WeatherAlert(
        1,
        0.0.toLong(),
        System.currentTimeMillis() + 50L,
        0.0.toLong(),
        System.currentTimeMillis()
    )

    private val alert2 = WeatherAlert(
        2,
        0.0.toLong(),
        System.currentTimeMillis() + 50L,
        0.0.toLong(),
        System.currentTimeMillis()
    )

    private val alert3 = WeatherAlert(
        2,
        0.0.toLong(),
        System.currentTimeMillis() - 50L,
        0.0.toLong(),
        -50L
    )

    @Before
    fun setupViewModel() {
        repository = FakeRepo()
        repository.alertData.add(alert1)
        viewModel = SharedAlertViewModel(repository)
    }

    @Test
    fun getAllAlerts_returnAllAlerts() {
        // When
        val value = viewModel.getAllAlerts().getOrAwaitValue()

        // Then
        assertThat(value, not(nullValue()))
        assertEquals(value[0], alert1)
    }


    @Test
    fun saveWeatherAlert_returnTrue() {
        var result = false

        runBlockingTest {
            val collectJob = launch {
                viewModel.alertInsertedSuccess.collect {
                    result = it
                }
            }

            viewModel.saveWeatherAlert(alert2)
            collectJob.cancel()
        }

        assertEquals(true, result)
    }

    @Test
    fun deleteAlert_returnId() {
        var result = 0

        runBlockingTest {
            val collectJob = launch {
                viewModel.deletedAlertId.collect {
                    result = it
                }
            }

            viewModel.deleteAlert(alert2)
            collectJob.cancel()
        }

        assertEquals(alert2.id, result)
    }

    @Test
    fun deleteAlerts_shouldDeleteAlert3() {
        //Given
        repository.alertData.add(alert3)

        // When
        viewModel.deleteAlerts()

        // Then
        val value = viewModel.getAllAlerts().getOrAwaitValue()
        assertThat(value, not(nullValue()))
        assertEquals(value.size, 1)
    }
}