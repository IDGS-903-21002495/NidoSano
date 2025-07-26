package com.modulap.nidosano.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
// REMOVED: import androidx.core.content.ContextCompat.getSystemService // Eliminar esta importación
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.modulap.nidosano.MainActivity
import com.modulap.nidosano.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirebaseMessagingService : FirebaseMessagingService() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    // private val notificationRepository = NotificationRepository() // Descomentado y asignado

    companion object {
        private const val TAG = "MyFirebaseMsgService" // Etiqueta más descriptiva
        const val CHANNEL_ID = "nidosano_alerts_channel" // ID para alertas
        const val CHANNEL_NAME = "Alertas Nido Sano"
        const val CHANNEL_DESCRIPTION = "Notificaciones de alertas importantes del gallinero"
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Revisar si el mensaje contiene datos
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val eventType = remoteMessage.data["eventType"]
            val title = remoteMessage.data["title"] ?: "Alerta Nido Sano"
            val message = remoteMessage.data["message"] ?: "Ha ocurrido un evento en tu gallinero"

            showNotification(title, message, eventType)
            // saveNotificationToFirestore(title, message, eventType) // Usar los mismos parámetros
        }

        // Revisar si el mensaje contiene una notificación
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")

            val title = it.title ?: "Alerta Nido Sano"
            val body = it.body ?: "Ha ocurrido un evento en tu gallinero"
            val eventType = remoteMessage.data["eventType"] // Aún puedes extraer datos personalizados aquí

            showNotification(title, body, eventType)
            // saveNotificationToFirestore(title, body, eventType) // Usar los mismos parámetros
        }
    }

    private fun showNotification(title: String, message: String, eventType: String?) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationEventType", eventType)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.gallina) // Asegúrate de que 'gallina' es un drawable válido
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@FirebaseMessagingService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "No se pudo mostrar la notificación: Permiso POST_NOTIFICATIONS no concedido.")
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        Log.d(TAG, "Sending token to server: $token")
        FirebaseFirestore.getInstance().collection("users").document("MVGCTaZFfuL7XyePLKzu").update("fcmToken", token)
    }

    /*
    private fun saveNotificationToFirestore(title: String, message: String, eventType: String?) {
        // Lógica para guardar la notificación en Firestore
        scope.launch {
            try {
                // Formatear la fecha/hora actual
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val notification = Notification(
                    title = title,
                    message = message,
                    timestamp = timestamp,
                    eventType = eventType ?: "unknown", // Usar "unknown" si eventType es nulo
                    read = false
                )
                notificationRepository.addNotification(notification)
                Log.d(TAG, "Notification saved to Firestore: $notification")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving notification to Firestore", e)
            }
        }
    }
     */
}