package com.example.weatherapp.di

import com.example.weatherapp.model.local.AlertDao
import com.example.weatherapp.model.local.HelperSharedPreferences
import com.example.weatherapp.model.local.WeatherDao
import com.example.weatherapp.model.remote.ApiService
import com.example.weatherapp.model.repos.Repo
import com.example.weatherapp.model.repos.RepoImpl
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