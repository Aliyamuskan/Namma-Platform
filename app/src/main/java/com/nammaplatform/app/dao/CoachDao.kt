package com.nammaplatform.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nammaplatform.app.entities.CoachEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoaches(coaches: List<CoachEntity>)

    @Query("SELECT * FROM coach_table WHERE trainId = :trainId")
    fun getCoachesForTrain(trainId: Int): Flow<List<CoachEntity>>
}