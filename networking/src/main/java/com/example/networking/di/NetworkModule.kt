package com.example.networking.di

import com.example.networking.json.JSONObjectAdapter
import com.example.networking.webservices.PhotosWebservice
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun ioCoroutineScope() = CoroutineScope(Dispatchers.IO)

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Singleton
    @Provides
    fun providesMoshi(): Moshi = Moshi.Builder()
        .add(JSONObjectAdapter())
        .build()

    @Provides
    fun provideRetrofit(
        moshi: Moshi,
        loggingInterceptor: HttpLoggingInterceptor,
    ): Retrofit {
        val builder = OkHttpClient.Builder()
        builder.callTimeout(30, TimeUnit.SECONDS)
        builder.retryOnConnectionFailure(true)
        builder.addInterceptor(BasicInterceptor())
        builder.addNetworkInterceptor(loggingInterceptor)
        val httpClient = builder.build()

        return Retrofit.Builder()
            .baseUrl("https://www.flickr.com/services/rest/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun providePhotoWebservice(retrofit: Retrofit): PhotosWebservice =
        retrofit.create(PhotosWebservice::class.java)

    // Uses Interceptor
    class BasicInterceptor() : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val builder = request.newBuilder()
            return chain.proceed(builder.build())
        }
    }
}