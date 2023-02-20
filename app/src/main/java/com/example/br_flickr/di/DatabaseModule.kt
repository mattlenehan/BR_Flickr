package com.example.br_flickr.di

import android.content.Context
import androidx.room.Room
import com.example.br_flickr.database.SearchHistoryDao
import com.example.br_flickr.database.SearchHistoryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): SearchHistoryDatabase {
        return Room.databaseBuilder(
            appContext,
            SearchHistoryDatabase::class.java,
            "Queries"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideChannelDao(searchHistoryDatabase: SearchHistoryDatabase): SearchHistoryDao {
        return searchHistoryDatabase.searchesDao
    }

}