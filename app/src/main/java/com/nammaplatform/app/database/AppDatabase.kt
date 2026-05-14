package com.nammaplatform.app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nammaplatform.app.dao.CoachDao
import com.nammaplatform.app.dao.FeedbackDao
import com.nammaplatform.app.dao.RecentStationDao
import com.nammaplatform.app.dao.TrainDao
import com.nammaplatform.app.entities.CoachEntity
import com.nammaplatform.app.entities.FeedbackEntity
import com.nammaplatform.app.entities.RecentStationEntity
import com.nammaplatform.app.entities.TrainEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Database(
    entities = [TrainEntity::class, CoachEntity::class, FeedbackEntity::class, RecentStationEntity::class],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trainDao(): TrainDao
    abstract fun coachDao(): CoachDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun recentStationDao(): RecentStationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "namma_rail_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(trainDao: TrainDao) {
            withContext(Dispatchers.IO) {
                if (trainDao.getAnyTrain() == null) {
                    val trains = listOf(
                        TrainEntity(
                            station = "Bengaluru",
                            trainName = "Shatabdi Express",
                            trainNumber = "12007",
                            platform = "5",
                            arrivalTime = "9:20 PM",
                            departureTime = "9:30 PM",
                            destination = "Chennai",
                            status = "Boarding",
                            coachSequence = "Engine,C1,C2,EXEC,LADIES"
                        ),
                        TrainEntity(
                            station = "Mysuru",
                            trainName = "Golgumbaz Express",
                            trainNumber = "16535",
                            platform = "2",
                            arrivalTime = "5:40 PM",
                            departureTime = "5:50 PM",
                            destination = "Solapur",
                            status = "On Time",
                            coachSequence = "Engine,General,General,S1,S2,LADIES"
                        ),
                        TrainEntity(
                            station = "Mysuru",
                            trainName = "Hampi Express",
                            trainNumber = "16591",
                            platform = "1",
                            arrivalTime = "6:15 PM",
                            departureTime = "6:25 PM",
                            destination = "Hubballi",
                            status = "Arriving Soon",
                            coachSequence = "Engine,General,S1,S2,S3,S4,S5,S6,B1,B2,A1,General"
                        ),
                        TrainEntity(
                            station = "Hubballi",
                            trainName = "Siddhaganga Exp",
                            trainNumber = "12725",
                            platform = "3",
                            arrivalTime = "1:00 PM",
                            departureTime = "1:10 PM",
                            destination = "Bengaluru",
                            status = "On Time",
                            coachSequence = "Engine,General,S1,S2,S3,S4,General"
                        ),
                        TrainEntity(
                            station = "Davangere",
                            trainName = "Intercity Exp",
                            trainNumber = "12726",
                            platform = "1",
                            arrivalTime = "2:30 PM",
                            departureTime = "2:40 PM",
                            destination = "Hubballi",
                            status = "On Time",
                            coachSequence = "Engine,General,S1,S2,General"
                        ),
                        TrainEntity(
                            station = "Tumakuru",
                            trainName = "Passenger",
                            trainNumber = "56221",
                            platform = "2",
                            arrivalTime = "4:00 PM",
                            departureTime = "4:10 PM",
                            destination = "Bengaluru",
                            status = "On Time",
                            coachSequence = "Engine,General,General,General"
                        )
                    )
                    trains.forEach { trainDao.insertTrain(it) }
                }
            }
        }
    }
}
