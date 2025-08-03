package com.modulap.nidosano.data.model

data class DailySummary(
    val timestamp: String,
    val temperature: String,
    val humidity: String,
    val airQuality: String,
    val lightingLevel: String
)

