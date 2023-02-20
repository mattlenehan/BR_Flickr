package com.example.networking.webservices

import com.example.models.PhotosResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotosWebservice {
    @GET("?method=flickr.photos.search")
    suspend fun getPhotos(
        @Query("text") text: String,
        @Query("page") page: Int,
        @Query("api_key") apiKey: String = "1508443e49213ff84d566777dc211f2a",
        @Query("format") format: String? = "json",
        @Query("nojsoncallback") noJsonCallback: String = "1",
        @Query("safe_search") safeSearch: String = "1"
    ): Response<PhotosResponse>
}
