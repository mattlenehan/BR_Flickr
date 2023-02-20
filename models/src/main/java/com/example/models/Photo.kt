package com.example.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Photo(
    @Json(name = "id")
    val id: String,

    @Json(name = "owner")
    val owner: String,

    @Json(name = "secret")
    val secret: String,

    @Json(name = "server")
    val server: String,

    @Json(name = "farm")
    val farm: Int,

    @Json(name = "title")
    val title: String,

    @Json(name = "ispublic")
    val isPublic: Int,

    @Json(name = "isfriend")
    val isFriend: Int,

    @Json(name = "isfamily")
    val isFamily: Int,
)