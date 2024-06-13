package com.dev.weatherapp.di

import com.dev.weatherapp.model.data.local.AlertDao
import com.dev.weatherapp.model.data.local.HelperSharedPreferences
import com.dev.weatherapp.model.data.local.WeatherDao
import com.dev.weatherapp.model.data.remote.ApiService
import com.dev.weatherapp.model.data.repos.Repo
import com.dev.weatherapp.model.data.repos.RepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMoviesRepository(
        apiService: ApiService,
        weatherDao: WeatherDao,
        alertDao: AlertDao,
    ): Repo {
        return RepoImpl(apiService, weatherDao, alertDao)
    }

}