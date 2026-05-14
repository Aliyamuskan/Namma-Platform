package com.nammaplatform.app.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_station_table")
data class RecentStationEntity(
    @PrimaryKey(autoGenerate = true)
    val stationId: Int = 0,
    val stationName: String
)