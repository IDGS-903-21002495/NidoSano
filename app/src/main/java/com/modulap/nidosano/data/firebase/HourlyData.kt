package com.modulap.nidosano.data.firebase

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.modulap.nidosano.data.model.HourlyRecord
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

suspend fun getHourlyDataForDate(userId: String, coopId: String, date: String): List<HourlyRecord> {
    val firestore = Firebase.firestore
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val targetDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)

    val snapshot = firestore.collection("users")
        .document(userId)
        .collection("chicken_coop")
        .document(coopId)
        .collection("sensors")
        .get()
        .await()

    return snapshot.documents
        .mapNotNull { doc ->
            val timestamp = doc.getTimestamp("date")?.toDate() ?: return@mapNotNull null
            if (SimpleDateFormat("yyyy-MM-dd").format(timestamp) != date) return@mapNotNull null

            val hourStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
            val temperature = doc.getDouble("temperature")?.let { String.format("%.1f°C", it) } ?: "-"
            val humidity = doc.getDouble("humidity")?.let { String.format("%.1f%%", it) } ?: "-"
            val lighting = doc.getString("lighting_level") ?: "-"
            val airQuality = doc.getString("air_quality") ?: "-"

            HourlyRecord(
                hour = hourStr,
                temperature = temperature,
                humidity = humidity,
                lightingLevel = lighting,
                airQuality = airQuality
            )
        }
        .sortedBy { it.hour }
}
