package com.example.br_flickr.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    // search history list
    @Query("select * from DatabaseSearchHistoryItem")
    fun getSearchQueries(): Flow<List<DatabaseSearchHistoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSearchQuery(databaseSearchHistoryItem: DatabaseSearchHistoryItem)
}

@Database(entities = [DatabaseSearchHistoryItem::class], version = 1, exportSchema = false)
abstract class SearchHistoryDatabase : RoomDatabase() {
    abstract val searchesDao: SearchHistoryDao
}