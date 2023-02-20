package com.example.br_flickr.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DatabaseSearchHistoryItem constructor(
    @PrimaryKey
    val id: Int,
    val query: String
)

fun List<DatabaseSearchHistoryItem>.asDomainModel(): List<String> {
    return map {
        it.query
    }
}