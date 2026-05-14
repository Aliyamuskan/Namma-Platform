package com.nammaplatform.app.repository

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.nammaplatform.app.models.FirebaseTrain
import com.nammaplatform.app.models.TrainInfo
import com.nammaplatform.app.util.JsonUtils
import com.nammaplatform.app.util.LanguageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class TrainRepository(private val context: Context) {

    private val database = FirebaseDatabase.getInstance().getReference("trains")
    private val TAG = "TrainRepository"

    companion object {
        private val departureFallbackCache = ConcurrentHashMap<String, String>()
        private val arrivalFallbackCache = ConcurrentHashMap<String, String>()
        private val trainIndexMap = ConcurrentHashMap<String, Int>()
        private val fallbackCounter = AtomicInteger(0)
    }

    private fun getCurrentLanguage(): String {
        return LanguageManager.getLanguage(context)
    }

    fun getLiveTrains(station: String): Flow<List<TrainInfo>> = channelFlow {
        val lang = getCurrentLanguage()
        val targetStation = station.trim()
        Log.i(TAG, "Starting live fetch. Target station: $targetStation, Language: $lang")

        val allLocalTrains = JsonUtils.getAllTrainsFromJson(context, lang)
        
        val initialProcessed = processAndApplyFallbacks(allLocalTrains, targetStation)
        send(initialProcessed)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fbTrainsMap = mutableMapOf<String, TrainInfo>()
                
                for (trainSnapshot in snapshot.children) {
                    try {
                        val fb = trainSnapshot.getValue(FirebaseTrain::class.java)
                        if (fb != null) {
                            val tNumber = fb.fetchNumber()
                            val departureValue = fb.fetchDeparture()
                            val arrivalValue = fb.fetchArrival()
                            val fbStatus = fb.fetchStatus(lang)

                            if (tNumber.isEmpty()) continue

                            val trainName = JsonUtils.getLocalizedTrainName(context, tNumber, lang)
                            val destination = JsonUtils.getLocalizedDestination(context, tNumber, lang)

                            fbTrainsMap[tNumber] = TrainInfo(
                                number = tNumber,
                                name = trainName,
                                platform = fb.fetchPlatform(),
                                arrivalTime = arrivalValue,
                                departureTime = departureValue,
                                status = fbStatus,
                                coaches = JsonUtils.getCoachLayoutFromJson(context, tNumber),
                                destination = destination,
                                sourceStation = fb.fetchStation()
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing Firebase train node", e)
                    }
                }

                val mergedList = allLocalTrains.map { local ->
                    fbTrainsMap[local.number]?.let { fbUpdate ->
                        fbUpdate.copy(
                            name = if (fbUpdate.name.isBlank() || fbUpdate.name == "Unknown Train") local.name else fbUpdate.name,
                            destination = if (fbUpdate.destination.isBlank() || fbUpdate.destination == "Unknown") local.destination else fbUpdate.destination,
                            coaches = if (fbUpdate.coaches.isEmpty()) local.coaches else fbUpdate.coaches,
                            arrivalTime = if (isPlaceholder(fbUpdate.arrivalTime)) local.arrivalTime else fbUpdate.arrivalTime,
                            departureTime = if (isPlaceholder(fbUpdate.departureTime)) {
                                if (isPlaceholder(local.departureTime)) "" else local.departureTime
                            } else fbUpdate.departureTime,
                            platform = if (fbUpdate.platform.isBlank() || fbUpdate.platform == "TBD") local.platform else fbUpdate.platform,
                            sourceStation = if (fbUpdate.sourceStation.isBlank()) local.sourceStation else fbUpdate.sourceStation
                        )
                    } ?: local
                }.toMutableList()

                fbTrainsMap.forEach { (num, fbTrain) ->
                    if (mergedList.none { it.number == num }) {
                        mergedList.add(fbTrain)
                    }
                }

                val finalResult = processAndApplyFallbacks(mergedList, targetStation)
                trySend(finalResult)
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.addValueEventListener(listener)
        awaitClose { database.removeEventListener(listener) }
    }.flowOn(Dispatchers.IO)

    fun getTrainByNumber(trainNumber: String): Flow<TrainInfo?> {
        return getLiveTrains("").map { trains ->
            trains.find { it.number == trainNumber }
        }
    }

    private fun processAndApplyFallbacks(trains: List<TrainInfo>, targetStation: String): List<TrainInfo> {
        // Filter trains by the selected station if a target station is specified
        val filtered = if (targetStation.isNotBlank()) {
            trains.filter { it.sourceStation.equals(targetStation, ignoreCase = true) }
        } else {
            trains
        }

        // Project Requirement: Select the nearest upcoming trains based on timing.
        // We sort primarily by time to ensure the soonest arriving trains are at the top.
        val sorted = filtered.sortedBy { parseTimeToMinutes(it.arrivalTime) }

        return sorted.map { train ->
            var arr = train.arrivalTime.trim()
            var dep = train.departureTime.trim()

            if (isPlaceholder(arr)) {
                arr = getOrCreateFallback(train.number, isDeparture = false)
            }
            
            if (isPlaceholder(dep)) {
                dep = generateRelativeDeparture(arr)
            }
            
            train.copy(departureTime = dep, arrivalTime = arr)
        }
    }

    private fun generateRelativeDeparture(arrivalTime: String): String {
        if (isPlaceholder(arrivalTime)) return arrivalTime
        val formats = listOf("h:mm a", "hh:mm a", "H:mm", "HH:mm")
        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                val date = sdf.parse(arrivalTime.uppercase())
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.add(Calendar.MINUTE, 5)
                    return SimpleDateFormat("h:mm a", Locale.ENGLISH).format(calendar.time)
                }
            } catch (e: Exception) {
                continue
            }
        }
        return arrivalTime
    }

    private fun getOrCreateFallback(trainNumber: String, isDeparture: Boolean): String {
        val cache = if (isDeparture) departureFallbackCache else arrivalFallbackCache
        return cache.computeIfAbsent(trainNumber) {
            val index = trainIndexMap.computeIfAbsent(trainNumber) { fallbackCounter.getAndIncrement() }
            val calendar = Calendar.getInstance()
            val baseOffset = if (isDeparture) 15 else 5
            calendar.add(Calendar.MINUTE, baseOffset + (index * 7))
            SimpleDateFormat("h:mm a", Locale.ENGLISH).format(calendar.time)
        }
    }

    private fun isPlaceholder(time: String): Boolean {
        val t = time.trim()
        return t.isEmpty() || t == "--:--" || t == "N/A" || t == "null" || t.contains("--")
    }

    private fun parseTimeToMinutes(time: String): Int {
        return try {
            val t = time.uppercase().trim()
            if (isPlaceholder(t)) return Int.MAX_VALUE
            
            val isPM = t.contains("PM")
            val isAM = t.contains("AM")
            val cleanTime = t.replace("AM", "").replace("PM", "").trim()
            val parts = cleanTime.split(":")
            var h = parts[0].toInt()
            val m = if (parts.size > 1) parts[1].toInt() else 0

            if (isPM && h < 12) h += 12
            if (isAM && h == 12) h = 0
            h * 60 + m
        } catch (e: Exception) {
            Int.MAX_VALUE
        }
    }
}
