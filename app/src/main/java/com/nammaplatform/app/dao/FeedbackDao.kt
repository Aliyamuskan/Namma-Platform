package com.nammaplatform.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nammaplatform.app.entities.FeedbackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: FeedbackEntity)

    @Query("SELECT * FROM feedback_table ORDER BY feedbackId DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>
}