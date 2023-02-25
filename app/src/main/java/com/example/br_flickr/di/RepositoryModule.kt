package com.example.br_flickr.di

import com.example.br_flickr.database.SearchHistoryDatabase
import com.example.networking.webservices.PhotosWebservice
import com.example.br_flickr.repository.PhotoRepository
import com.example.br_flickr.repository.PhotoRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun providePhotoRepository(
        photosWebservice: PhotosWebservice,
        database: SearchHistoryDatabase,
        retrofit: Retrofit,
        coroutineScope: CoroutineScope
    ): PhotoRepository {
        return PhotoRepositoryImpl(
            photosWebservice = photosWebservice,
            database = database,
            retrofit = retrofit,
            coroutineScope = coroutineScope
        )
    }
}