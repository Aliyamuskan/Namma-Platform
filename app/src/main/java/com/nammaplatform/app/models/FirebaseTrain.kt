package com.nammaplatform.app.models

import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.PropertyName

@IgnoreExtraProperties
class FirebaseTrain {
    @PropertyName("trainNumber") var trainNumber: Any? = null
    @PropertyName("train_number") var train_number_snake: Any? = null
    @PropertyName("station") var station: Any? = null
    @PropertyName("platform") var platform: Any? = null
    @PropertyName("arrivalTime") var arrivalTime: Any? = null
    @PropertyName("arrival_time") var arrival_time_snake: Any? = null
    @PropertyName("departureTime") var departureTime: Any? = null
    @PropertyName("departure_time") var departure_time_snake: Any? = null
    @PropertyName("departureTiming") var departureTiming: Any? = null
    @PropertyName("departure_timing") var departure_timing: Any? = null
    @PropertyName("status") var status: Any? = null
    @PropertyName("status_en") var status_en: Any? = null
    @PropertyName("status_kn") var status_kn: Any? = null
    @PropertyName("status_hi") var status_hi: Any? = null

    fun fetchNumber(): String = (trainNumber ?: train_number_snake ?: "").toString().trim()
    fun fetchArrival(): String = (arrivalTime ?: arrival_time_snake ?: "").toString().trim()
    
    fun fetchDeparture(): String {
        val d = departureTime ?: departure_time_snake ?: departureTiming ?: departure_timing ?: ""
        return d.toString().trim()
    }

    fun fetchStation(): String = (station ?: "").toString().trim()
    fun fetchPlatform(): String = (platform ?: "").toString().trim()
    
    fun fetchStatus(lang: String): String {
        val s = when (lang) {
            "kn" -> status_kn ?: status
            "hi" -> status_hi ?: status
            else -> status_en ?: status
        }
        return (s ?: "").toString().trim()
    }
}
