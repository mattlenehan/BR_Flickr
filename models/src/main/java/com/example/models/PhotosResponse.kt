package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PhotosResponse(
    @Json(name = "photos")
    val photoPage: PhotoPage?,

    @Json(name = "stat")
    val stat: String,
)