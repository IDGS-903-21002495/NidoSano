package com.modulap.nidosano.data.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.modulap.nidosano.data.model.FeedingSchedule
import kotlinx.coroutines.tasks.await

object FirestoreManager {
    private const val TAG = "FirestoreManager"
    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_USERS = "users"
    private const val SUBCOLLECTION_CHICKEN_COOP = "chicken_coop"
    private const val DOCUMENT_DEFAULT_CHICKEN_COOP = "defaultChickenCoop"
    private const val SUBCOLLECTION_FEEDING_SCHEDULES = "feeding_schedules"

    private fun getFeedingSchedulesCollectionRef(userId: String) =
        db.collection(COLLECTION_USERS)
            .document(userId)
            .collection(SUBCOLLECTION_CHICKEN_COOP)
            .document(DOCUMENT_DEFAULT_CHICKEN_COOP)
            .collection(SUBCOLLECTION_FEEDING_SCHEDULES)

    suspend fun saveFeedingSchedule(userId: String, schedule: FeedingSchedule): Boolean {
        if (userId.isBlank()) {
            Log.e(TAG, "UserId no puede estar vacío para guardar horario.")
            return false
        }
        if (schedule.id.isBlank()) {
            Log.e(TAG, "Schedule ID no puede estar vacío para guardar horario.")
            return false
        }

        return try {
            getFeedingSchedulesCollectionRef(userId)
                .document(schedule.id)
                .set(schedule)
                .await()
            Log.d(TAG, "Horario guardado/actualizado en Firestore: ${schedule.id} para usuario $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar horario en Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun deleteFeedingSchedule(userId: String, scheduleId: String): Boolean {
        if (userId.isBlank() || scheduleId.isBlank()) {
            Log.e(TAG, "UserId o Schedule ID no pueden estar vacíos para eliminar horario.")
            return false
        }

        return try {
            getFeedingSchedulesCollectionRef(userId)
                .document(scheduleId)
                .delete()
                .await()
            Log.d(TAG, "Horario eliminado de Firestore: $scheduleId para usuario $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar horario de Firestore: ${e.message}", e)
            false
        }
    }

    suspend fun getFeedingSchedules(userId: String): List<FeedingSchedule> {
        if (userId.isBlank()) {
            Log.e(TAG, "UserId no puede estar vacío para obtener horarios.")
            return emptyList()
        }

        return try {
            val snapshot = getFeedingSchedulesCollectionRef(userId)
                .get()
                .await()
            snapshot.documents.mapNotNull { document ->
                document.toObject(FeedingSchedule::class.java)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener horarios de Firestore: ${e.message}", e)
            emptyList()
        }
    }
}