package com.modulap.nidosano.data.firebase

import android.annotation.SuppressLint
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.modulap.nidosano.data.model.DailySummary
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale


@SuppressLint("DefaultLocale")
suspend fun getLastDailySummaries(
    userId: String,
    coopId: String
): List<DailySummary> {
    val firestore = Firebase.firestore
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val snapshot = firestore.collection("users")
        .document(userId)
        .collection("chicken_coop")
        .document(coopId)
        .collection("sensors")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // más nuevo primero
        .get()
        .await()

    val seenDates = mutableSetOf<String>()
    val summaries = mutableListOf<DailySummary>()

    for (doc in snapshot.documents) {
        val timestamp = doc.getTimestamp("timestamp") ?: continue
        val temperature = doc.getDouble("temperature") ?: continue
        val humidity = doc.getDouble("humidity") ?: continue
        val airQuality = doc.getString("air_quality") ?: "-"
        val lighting = doc.getString("lighting_level") ?: "-"

        val dateStr = formatter.format(timestamp.toDate())

        if (seenDates.add(dateStr)) {
            summaries.add(
                DailySummary(
                    timestamp = dateStr,
                    temperature = String.format("%.1f°C", temperature),
                    humidity = String.format("%.1f%%", humidity),
                    airQuality = airQuality,
                    lightingLevel = lighting
                )
            )
        }
    }

    return summaries
}
