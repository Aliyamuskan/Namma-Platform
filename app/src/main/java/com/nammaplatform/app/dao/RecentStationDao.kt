package com.nammaplatform.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nammaplatform.app.entities.RecentStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentStationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentStation(station: RecentStationEntity)

    @Query("SELECT * FROM recent_station_table ORDER BY stationId DESC LIMIT 5")
    fun getRecentStations(): Flow<List<RecentStationEntity>>
}