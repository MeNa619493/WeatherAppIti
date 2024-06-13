package com.dev.weatherapp.di

import android.content.Context
import androidx.room.Room
import com.dev.weatherapp.model.data.local.HelperSharedPreferences
import com.dev.weatherapp.model.data.local.WeatherDatabase
import com.dev.weatherapp.utils.Utils.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {

    @Provides
    @Singleton
    fun provideHelperSharedPreferences(@ApplicationContext context: Context): HelperSharedPreferences {
        return HelperSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        WeatherDatabase::class.java,
        DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideWeatherDao(database: WeatherDatabase) = database.weatherDao()

    @Singleton
    @Provides
    fun provideAlertDao(database: WeatherDatabase) = database.alertDao()

}