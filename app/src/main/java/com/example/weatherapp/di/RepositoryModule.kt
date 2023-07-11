package com.example.weatherapp.di

import com.example.weatherapp.model.data.local.AlertDao
import com.example.weatherapp.model.data.local.HelperSharedPreferences
import com.example.weatherapp.model.data.local.WeatherDao
import com.example.weatherapp.model.data.remote.ApiService
import com.example.weatherapp.model.data.repos.Repo
import com.example.weatherapp.model.data.repos.RepoImpl
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