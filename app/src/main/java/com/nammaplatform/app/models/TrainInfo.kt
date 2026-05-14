package com.nammaplatform.app.models

data class TrainInfo(
    val id: Int = -1,
    val number: String,
    val name: String,
    val platform: String,
    val arrivalTime: String,
    val departureTime: String,
    val destination: String = "N/A",
    val status: String,
    val sourceStation: String = "", // Added to support station-specific lists
    val coaches: List<String> = emptyList()
)
