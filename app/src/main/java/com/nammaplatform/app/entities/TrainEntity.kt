package com.nammaplatform.app.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "train_table")
data class TrainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val station: String,
    val trainName: String,
    val trainNumber: String,
    val platform: String,
    val arrivalTime: String,
    val departureTime: String = "N/A",
    val destination: String = "N/A",
    val status: String,
    val coachSequence: String = "Engine,General,S1,S2,S3,General"
)
