package com.nammaplatform.app.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coach_table")
data class CoachEntity(
    @PrimaryKey(autoGenerate = true)
    val coachId: Int = 0,
    val trainId: Int,
    val coachName: String,
    val coachType: String
)