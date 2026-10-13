package com.franklider.app.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(location: LocationRecord)

    @Query("DELETE FROM locations")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun count(): Int
}
