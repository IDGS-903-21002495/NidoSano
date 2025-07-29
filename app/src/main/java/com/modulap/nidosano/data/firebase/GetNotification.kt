package com.modulap.nidosano.data.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.modulap.nidosano.data.model.Notification
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("DefaultLocal")
suspend fun  getNotification(
    userId: String,
    coopId: String,
): List<Notification>{
     val firestore = Firebase.firestore
     val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())


    val snapshot = firestore.collection("users")
        .document(userId)
        .collection("chicken_coop")
        .document(coopId)
        .collection("alerts")
        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // más nuevo primero
        .get()
        .await()

    Log.d("getNotification", "Docs count: ${snapshot.documents.size}")


    val seenDates = mutableSetOf<String>()
    val notifications = mutableListOf<Notification>()

    for (doc in snapshot.documents){
        val time = doc.getTimestamp("timestamp") ?: continue
        val titles = doc.getString("title") ?: continue
        val type = doc.getString("type") ?:continue
        val descriptions = doc.getString("description") ?:continue

        val dateStr = formatter.format(time.toDate())

        notifications.add(
            Notification(
                title = titles,
                type = type,
                description = descriptions,
                time = dateStr

            )
        )
    }

return  notifications


}