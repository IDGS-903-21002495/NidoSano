package com.modulap.nidosano.data.firebase


import com.modulap.nidosano.data.model.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

suspend fun getUser(
    userId: String,
): User? {

    val firestore = Firebase.firestore


    val snapshot = firestore.collection("users")
        .document(userId)
        .get()
        .await()

    return snapshot.toObject(User::class.java)
}