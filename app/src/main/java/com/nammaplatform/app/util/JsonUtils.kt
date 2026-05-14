package com.nammaplatform.app.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nammaplatform.app.models.TrainInfo
import java.io.IOException

object JsonUtils {
    
    private fun getJsonData(context: Context): List<Map<String, Any>> {
        return try {
            val jsonString = context.assets.open("trains.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
            Gson().fromJson(jsonString, listType)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getAllTrainsFromJson(context: Context, lang: String = "en"): List<TrainInfo> {
        val trains = getJsonData(context)
        return trains.map { map ->
            TrainInfo(
                number = (map["train_number"] as? String) ?: "",
                name = (map["name_$lang"] as? String) ?: (map["name_en"] as? String) ?: "Unknown",
                platform = (map["platform"] as? String) ?: "TBD",
                arrivalTime = (map["arrival_time"] as? String) ?: "",
                departureTime = "",
                status = (map["status_$lang"] as? String) ?: (map["status_en"] as? String) ?: "Scheduled",
                destination = (map["destination_$lang"] as? String) ?: (map["destination_en"] as? String) ?: "Unknown",
                sourceStation = (map["source_station"] as? String) ?: "",
                coaches = (map["coaches"] as? List<String>) ?: emptyList()
            )
        }
    }

    fun getCoachLayoutFromJson(context: Context, trainNumber: String): List<String> {
        val trains = getJsonData(context)
        val train = trains.find { it["train_number"] == trainNumber }
        return (train?.get("coaches") as? List<String>) ?: emptyList()
    }

    fun getLocalizedTrainName(context: Context, trainNumber: String, lang: String): String {
        val trains = getJsonData(context)
        val train = trains.find { it["train_number"] == trainNumber }
        return (train?.get("name_$lang") as? String) ?: (train?.get("name_en") as? String) ?: "Unknown Train"
    }

    fun getLocalizedDestination(context: Context, trainNumber: String, lang: String): String {
        val trains = getJsonData(context)
        val train = trains.find { it["train_number"] == trainNumber }
        return (train?.get("destination_$lang") as? String) ?: (train?.get("destination_en") as? String) ?: "N/A"
    }
}
