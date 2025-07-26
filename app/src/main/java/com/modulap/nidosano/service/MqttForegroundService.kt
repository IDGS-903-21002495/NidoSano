package com.modulap.nidosano.service

import android.Manifest // Corregido: Importación correcta
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service // Corregido: Importación correcta
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.modulap.nidosano.MainActivity
import com.modulap.nidosano.R
import com.modulap.nidosano.data.model.NotificationRecord
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.data.repository.MqttMessage // Importación necesaria para MqttMessage
import com.modulap.nidosano.ui.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MqttForegroundService : Service() {
    companion object {
        private const val TAG = "MqttForegroundService"
        const val FOREGROUND_CHANNEL_ID = "nidosano_service_channel"
        const val FOREGROUND_CHANNEL_NAME = "Servicio MQTT Activo"
        const val FOREGROUND_CHANNEL_DESCRIPTION = "Mantiene la conexión MQTT activa para notificaciones."

        const val ALERT_CHANNEL_ID = "nidosano_alerts_channel"
        const val ALERT_CHANNEL_NAME = "Alertas Nido Sano"
        const val ALERT_CHANNEL_DESCRIPTION = "Notificaciones de alertas importantes del gallinero."

        const val FOREGROUND_NOTIFICATION_ID = 1
        // IDs únicos para cada tipo de notificación de alerta
        const val MOVEMENT_NOTIFICATION_ID = 2
        const val TEMPERATURE_NOTIFICATION_ID = 3
        const val HUMIDITY_NOTIFICATION_ID = 4
        const val AIR_QUALITY_NOTIFICATION_ID = 5
        const val LIGHTING_LEVEL_NOTIFICATION_ID = 6
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var firestore: FirebaseFirestore

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MqttForegroundService onCreate")
        notificationManager = NotificationManagerCompat.from(this)
        firestore = FirebaseFirestore.getInstance()
        createNotificationChannels()
        startForegroundServiceNotification()
        MQTTManagerHiveMQ.conectar()
        listenToMqttMessages()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fgChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                FOREGROUND_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = FOREGROUND_CHANNEL_DESCRIPTION
            }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = ALERT_CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = getColor(R.color.purple_200) // Asegúrate que este color exista o cámbialo
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(fgChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun listenToMqttMessages() {
        serviceScope.launch {
            MQTTManagerHiveMQ.messagesFlow.collect { message ->
                Log.d(TAG, "Mensaje MQTT recibido en servicio: ${message.topic} - ${message.payload}")

                handleMqttMessage(message)
            }
        }
    }

    // *** INICIO DE LA LÓGICA DE NOTIFICACIONES REQUERIDA Y ADAPTADA A TU ESTRUCTURA ***
    private fun handleMqttMessage(message: MqttMessage) {
        Log.d(TAG, "Processing MQTT message: Topic='${message.topic}', Payload='${message.payload}'")

        when (message.topic) {
            "movement/alert" -> {
                if (message.payload.isNotBlank() && message.payload != "Movimiento detectado en: 0") {
                    showNotification(
                        MOVEMENT_NOTIFICATION_ID,
                        "¡Alerta de Movimiento!", // Titulo
                        message.payload, // Mensaje
                        Routes.Security // Pantalla a la que se dirige
                    )
                    saveNotificationToFirestore(
                        title = "Alerta de Movimiento",
                        message = message.payload,
                        topic = message.topic,
                        payload = message.payload,
                        type = "Movimiento",
                        destinationRoute = Routes.Security
                    )
                }
            }
            "temperature" -> {
                if (message.payload.isNotBlank() && message.payload != "") {
                    try {
                        val temperature = message.payload.toDouble()
                        val umbralTemperaturaAlta = 35.0
                        val umbralTemperaturaBaja = 10.0

                        if (temperature > umbralTemperaturaAlta) {
                            showNotification(
                                TEMPERATURE_NOTIFICATION_ID, // Usar ID único
                                "¡Alerta: Temperatura Alta!",
                                "Temperatura actual: ${temperature}°C. Considera ventilar.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Temperatura Alta!",
                                message = "Temperatura actual: ${temperature}°C. Considera ventilar.",
                                topic = message.topic,
                                payload = message.payload,
                                type = "Temperatura",
                                destinationRoute = "home"
                            )
                        } else if (temperature < umbralTemperaturaBaja) {
                            showNotification(
                                TEMPERATURE_NOTIFICATION_ID,
                                "¡Alerta: Temperatura Baja!",
                                "Temperatura actual: ${temperature}°C. Podría hacer frío.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Temperatura Baja!",
                                message = "Temperatura actual: ${temperature}°C. Podría hacer frío.",
                                topic = message.topic,
                                payload = message.payload,
                                type = "Temperatura",
                                destinationRoute = "home"
                            )
                        } else {
                            Log.d(TAG, "Temperatura normal: $temperature°C")
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error al convertir temperatura a número: ${message.payload}", e)
                    }
                } else {
                    Log.w(TAG, "Payload de temperatura vacío o inválido: '${message.payload}'")
                }
            }
            "humidity" -> {
                if (message.payload.isNotBlank() && message.payload != "") {
                    try {
                        val humidity = message.payload.toDouble()
                        val umbralHumedadAlta = 65.0 // Porcentaje
                        val umbralHumedadBaja = 40.0 // Porcentaje

                        if (humidity > umbralHumedadAlta) {
                            showNotification(
                                HUMIDITY_NOTIFICATION_ID, // Usar ID único
                                "¡Alerta: Humedad Alta!",
                                "Humedad actual: ${humidity}%. Considera ventilar o deshumidificar.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Humedad Alta!",
                                message = "Humedad actual: ${humidity}%. Considera ventilar o deshumidificar.",
                                topic = message.topic,
                                payload = message.payload,
                                type = "Humedad",
                                destinationRoute = "home"
                            )
                        } else if (humidity < umbralHumedadBaja) {
                            showNotification(
                                HUMIDITY_NOTIFICATION_ID,
                                "¡Alerta: Humedad Baja!",
                                "Humedad actual: ${humidity}%. Podría ser necesario humidificar.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Humedad Baja!",
                                message = "Humedad actual: ${humidity}%. Podría ser necesario humidificar.",
                                topic = message.topic,
                                payload = message.payload,
                                type = "Humedad",
                                destinationRoute = "home"
                            )
                        } else {
                            Log.d(TAG, "Humedad normal: ${humidity}%")
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error al convertir humedad a número: ${message.payload}", e)
                    }
                } else {
                    Log.w(TAG, "Payload de humedad vacío o inválido: '${message.payload}'")
                }
            }
            "air_quality" -> {
                if (message.payload.isNotBlank() && message.payload != "") {
                    try {
                        val airQuality = message.payload.toDouble()
                        val umbralCalidadAireMala = 1000.0

                        if (airQuality > umbralCalidadAireMala) {
                            showNotification(
                                AIR_QUALITY_NOTIFICATION_ID, // Usar ID único
                                "¡Alerta: Calidad del Aire Baja!",
                                "Calidad del aire: ${airQuality}. Considera la ventilación.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "Alerta: Calidad del Aire Baja",
                                message = "Calidad del aire: ${airQuality}. Considera la ventilación.",
                                topic = message.topic,
                                payload = message.payload,
                                type = "Movimiento",
                                destinationRoute = "home"
                            )
                        } else {
                            Log.d(TAG, "Calidad del aire normal: ${airQuality}")
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error al convertir calidad del aire a número: ${message.payload}", e)
                    }
                } else {
                    Log.w(TAG, "Payload de calidad del aire vacío o inválido: '${message.payload}'")
                }
            }

            else -> {
                Log.d(TAG, "Mensaje de tópico no manejado: ${message.topic}")
            }
        }
    }



    private fun showNotification(id: Int, title: String, message: String, destinationRoute: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notificationDestinationRoute", destinationRoute)
            putExtra("notificationEventType", title)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.gallina_1_)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "Permiso POST_NOTIFICATIONS no concedido. No se pudo mostrar la notificación de alerta.")
                return
            }
        }
        notificationManager.notify(id, notification)
    }

    // Guardar notificación en Firestore
    private fun saveNotificationToFirestore(
        title: String,
        message: String,
        topic: String,
        payload: String,
        type: String,
        destinationRoute: String
    ) {

        val notificationRecord = NotificationRecord(
            title = title,
            description = message,
            type = type,
            topic = topic,
            payload = payload,
            destinationRoute = destinationRoute,
            isRead = false,
            userId = "MVGCTaZFfuL7XyePLKzu",
            chickenCoopId = "4MMnL8kHxbSV3ZplXThc"
        )

        // Construir la ruta dinámica
        val alertsCollectionRef = firestore.collection("users")
            .document("MVGCTaZFfuL7XyePLKzu")
            .collection("chicken_coop")
            .document("4MMnL8kHxbSV3ZplXThc")
            .collection("alerts")

        alertsCollectionRef.add(notificationRecord)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Notificación guardada en Firestore con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar notificación en Firestore: ${e.message}", e)
            }
    }

    private fun startForegroundServiceNotification() {
        val notification = NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Nido Sano: Monitoreo activo")
            .setContentText("El sistema está monitoreando el gallinero y enviando alertas.")
            .setSmallIcon(R.drawable.gallina_1_)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
        Log.d(TAG, "Servicio en primer plano iniciado.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "MqttForegroundService onStartCommand")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.coroutineContext.cancel()
        MQTTManagerHiveMQ.desconectar()
        Log.d(TAG, "MqttForegroundService onDestroy. Coroutines cancelled and MQTT disconnected.")
    }
}