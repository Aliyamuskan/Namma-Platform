package com.nammaplatform.app.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feedback_table")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val feedbackId: Int = 0,
    val rating: Float,
    val feedbackText: String
)