package io.github.konkonFox.iclmushroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: LocalItem)

    @Update
    suspend fun update(item: LocalItem)

    @Delete
    suspend fun delete(item: LocalItem)

    @Query("SELECT * from items ORDER BY createdAt ASC")
    fun getAllItems(): Flow<List<LocalItem>>
}