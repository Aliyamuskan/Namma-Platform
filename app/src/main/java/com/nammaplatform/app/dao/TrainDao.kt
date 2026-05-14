package com.nammaplatform.app.dao

import androidx.room.*
import com.nammaplatform.app.entities.TrainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrain(train: TrainEntity)

    @Query("SELECT * FROM train_table WHERE station = :station ORDER BY arrivalTime ASC")
    fun getTrainsByStation(station: String): Flow<List<TrainEntity>>

    @Query("SELECT * FROM train_table ORDER BY arrivalTime ASC LIMIT 3")
    fun getNextThreeTrains(): Flow<List<TrainEntity>>

    @Query("SELECT * FROM train_table WHERE station = :station ORDER BY arrivalTime ASC LIMIT 3")
    fun getNextThreeTrainsByStation(station: String): Flow<List<TrainEntity>>

    @Query("SELECT * FROM train_table WHERE trainNumber LIKE '%' || :query || '%' OR trainName LIKE '%' || :query || '%' ORDER BY arrivalTime ASC")
    fun searchTrains(query: String): Flow<List<TrainEntity>>

    @Query("SELECT * FROM train_table WHERE station = :station AND (trainNumber LIKE '%' || :query || '%' OR trainName LIKE '%' || :query || '%') ORDER BY arrivalTime ASC")
    fun searchTrainsByStation(station: String, query: String): Flow<List<TrainEntity>>

    @Query("SELECT * FROM train_table WHERE id = :trainId")
    suspend fun getTrainById(trainId: Int): TrainEntity?
    
    @Query("SELECT * FROM train_table WHERE trainName = :name LIMIT 1")
    suspend fun getTrainByName(name: String): TrainEntity?

    @Query("SELECT * FROM train_table LIMIT 1")
    suspend fun getAnyTrain(): TrainEntity?
}
