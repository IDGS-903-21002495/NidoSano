package com.modulap.nidosano.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import com.modulap.nidosano.data.repository.MqttMessage
import com.modulap.nidosano.ui.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first

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
        const val MOVEMENT_NOTIFICATION_ID = 2
        const val TEMPERATURE_NOTIFICATION_ID = 3
        const val HUMIDITY_NOTIFICATION_ID = 4
        const val AIR_QUALITY_NOTIFICATION_ID = 5
        const val LIGHTING_LEVEL_NOTIFICATION_ID = 6
        const val FOOD_LEVEL_NOTIFICATION_ID = 7
        const val WATER_LEVEL_NOTIFICATION_ID = 8

        const val EXTRA_USER_ID = "extra_user_id"
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var firestore: FirebaseFirestore

    private var currentUserId: String? = null
    private var reconnectJob: Job? = null
    private var hasPublishedUserIdToEspForCurrentSession: Boolean = false // Nueva bandera a nivel de servicio

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "MqttForegroundService onCreate")
        notificationManager = NotificationManagerCompat.from(this)
        firestore = FirebaseFirestore.getInstance()
        createNotificationChannels()
        startForegroundServiceNotification()
        listenToMqttMessages()
        observeMqttConnectionState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userIdFromIntent = intent?.getStringExtra(EXTRA_USER_ID)
        Log.d(TAG, "MqttForegroundService onStartCommand: userId from intent = $userIdFromIntent, currentUserId = $currentUserId")

        if (userIdFromIntent != null && userIdFromIntent != currentUserId) {
            Log.d(TAG, "onStartCommand: Nuevo userId detectado: $userIdFromIntent. Estableciendo currentUserId y conectando MQTT.")
            currentUserId = userIdFromIntent
            hasPublishedUserIdToEspForCurrentSession = false // Resetear bandera para el nuevo usuario
            MQTTManagerHiveMQ.conectar(currentUserId!!) // Conectar con el nuevo ID
            startReconnectLogic() // Iniciar lógica de reconexión si es necesario
        } else if (userIdFromIntent == null && currentUserId == null) {
            Log.w(TAG, "MqttForegroundService iniciado sin userId. Conectando sin ID específico inicialmente.")
            MQTTManagerHiveMQ.conectar() // Conectar sin userId si no hay ninguno
        } else if (userIdFromIntent == currentUserId && currentUserId != null) {
            Log.d(TAG, "onStartCommand: Mismo userId ($currentUserId). Asegurando que la conexión MQTT esté activa.")
            MQTTManagerHiveMQ.conectar(currentUserId!!) // Asegurar conexión activa
            startReconnectLogic()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MqttForegroundService onDestroy. Cancelando corrutinas y desconectando MQTT.")
        reconnectJob?.cancel()
        serviceScope.cancel()
        MQTTManagerHiveMQ.desconectar()
        hasPublishedUserIdToEspForCurrentSession = false // Limpiar la bandera al destruir el servicio
    }

    override fun onBind(intent: Intent?): IBinder? = null

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

    private fun observeMqttConnectionState() {
        serviceScope.launch {
            MQTTManagerHiveMQ.connectionStateFlow.collectLatest { (state, message) ->
                Log.d(TAG, "Estado de conexión MQTT observado en servicio: $state, $message")
                when (state) {
                    MQTTManagerHiveMQ.ConnectionState.DISCONNECTED,
                    MQTTManagerHiveMQ.ConnectionState.ERROR -> {
                        if (currentUserId != null) {
                            Log.e(TAG, "Cliente MQTT desconectado/error. Reiniciando lógica de reconexión.")
                            startReconnectLogic()
                        } else {
                            Log.w(TAG, "Cliente MQTT desconectado/error, pero no hay userId para reconectar. No iniciar reconexión automática sin ID.")
                        }
                        hasPublishedUserIdToEspForCurrentSession = false // Si se desconecta, el ESP puede necesitar el ID de nuevo
                    }
                    MQTTManagerHiveMQ.ConnectionState.CONNECTED -> {
                        reconnectJob?.cancel()
                        Log.d(TAG, "Cliente MQTT conectado, reconectJob cancelado.")
                        // --- AQUÍ es donde publicaremos el User ID al ESP ---
                        currentUserId?.let { userId ->
                            if (!hasPublishedUserIdToEspForCurrentSession) {
                                Log.d(TAG, "Conectado. Publicando userId al ESP: $userId en ${MQTTManagerHiveMQ.GLOBAL_USER_ID_CONFIG_TOPIC}")
                                MQTTManagerHiveMQ.publicar(
                                    MQTTManagerHiveMQ.GLOBAL_USER_ID_CONFIG_TOPIC,
                                    userId,
                                    qos = com.hivemq.client.mqtt.datatypes.MqttQos.AT_LEAST_ONCE,
                                    retain = true // Retain para que el ESP lo reciba incluso si se conecta más tarde
                                )
                                hasPublishedUserIdToEspForCurrentSession = true
                            } else {
                                Log.d(TAG, "userId ya publicado al ESP para esta sesión. No se republica.")
                            }
                        } ?: run {
                            Log.w(TAG, "Cliente conectado, pero currentUserId es nulo. No se pudo publicar el userId al ESP.")
                        }
                    }
                    MQTTManagerHiveMQ.ConnectionState.CONNECTING -> {
                        Log.d(TAG, "Cliente MQTT intentando conectar...")
                        // No es necesario iniciar reconexión aquí, ya está intentando
                    }
                }
            }
        }
    }

    private fun startReconnectLogic() {
        reconnectJob?.cancel() // Cancelar cualquier trabajo de reconexión anterior
        reconnectJob = serviceScope.launch {
            var attempt = 0
            while (isActive && currentUserId != null) {
                // Obtener el estado actual para evitar intentar reconectar si ya estamos conectados
                val (currentState, _) = MQTTManagerHiveMQ.connectionStateFlow.first()
                if (currentState == MQTTManagerHiveMQ.ConnectionState.CONNECTED) {
                    Log.d(TAG, "Reconexión exitosa o ya conectado. Saliendo del bucle de reconexión.")
                    break
                }

                val delayTime = minOf(1 shl attempt, 60) * 1000L // Retraso exponencial, máximo 60 segundos
                Log.d(TAG, "Intento de reconexión ${attempt + 1}. Reintentando en ${delayTime / 1000} segundos...")
                delay(delayTime)
                attempt++

                currentUserId?.let { userId ->
                    Log.d(TAG, "Intentando reconectar MQTT para userId: $userId")
                    MQTTManagerHiveMQ.conectar(userId)
                } ?: run {
                    Log.w(TAG, "No se puede reconectar: currentUserId es nulo durante el bucle de reconexión. Cancelando tarea de reconexión.")
                    reconnectJob?.cancel()
                    return@launch // Salir de la corrutina
                }
            }
            if (isActive && currentUserId != null) {
                val (finalState, _) = MQTTManagerHiveMQ.connectionStateFlow.first()
                if (finalState == MQTTManagerHiveMQ.ConnectionState.CONNECTED) {
                    Log.d(TAG, "Reconexión finalizada con éxito.")
                } else {
                    Log.d(TAG, "Reconexión finalizada sin éxito a pesar de intentos.")
                }
            } else {
                Log.d(TAG, "Reconexión finalizada debido a cancelación o userId nulo.")
            }
        }
    }

    private fun listenToMqttMessages() {
        serviceScope.launch {
            MQTTManagerHiveMQ.messagesFlow.collect { message ->
                Log.d(TAG, "Mensaje MQTT recibido en servicio (via flow): Tópico='${message.topic}', Payload='${message.payload}'")
                handleMqttMessage(message)
            }
        }
    }

    private fun handleMqttMessage(message: MqttMessage) {
        Log.d(TAG, "--- Inicia procesamiento de mensaje MQTT ---")
        Log.d(TAG, "Tópico recibido para procesamiento: '${message.topic}'")
        Log.d(TAG, "Payload recibido: '${message.payload}'")
        Log.d(TAG, "currentUserId en servicio: '$currentUserId'")

        if (currentUserId == null) {
            Log.w(TAG, "currentUserId es nulo. No se pueden procesar mensajes específicos de usuario. Fin del procesamiento.")
            return
        }

        val subTopic = message.topic

        Log.d(TAG, "SubTópico a procesar: '$subTopic'")

        when (subTopic) {
            "movement/alert" -> {
                Log.d(TAG, "SubTópico es 'movement/alert'.")
                if (message.payload.isNotBlank() && message.payload != "Movimiento detectado en: 0") {
                    Log.d(TAG, "¡ALERTA DE MOVIMIENTO DETECTADA! Payload: '${message.payload}'. Disparando notificación.")
                    showNotification(
                        MOVEMENT_NOTIFICATION_ID,
                        "¡Alerta de Movimiento!",
                        message.payload,
                        Routes.Security
                    )
                    saveNotificationToFirestore(
                        title = "Alerta de Movimiento",
                        message = message.payload,
                        topic = subTopic,
                        payload = message.payload,
                        type = "Movimiento",
                        destinationRoute = Routes.Security
                    )
                } else {
                    Log.d(TAG, "Payload de movimiento vacío o es 'Movimiento detectado en: 0'. No se dispara notificación.")
                }
            }
            "temperature" -> {
                Log.d(TAG, "SubTópico es 'temperature'.")
                if (message.payload.isNotBlank()) {
                    try {
                        val temperature = message.payload.toDouble()
                        val umbralTemperaturaAlta = 35.0
                        val umbralTemperaturaBaja = 10.0
                        Log.d(TAG, "Temperatura convertida: $temperature°C. Umbral Alto: $umbralTemperaturaAlta, Umbral Bajo: $umbralTemperaturaBaja")

                        if (temperature > umbralTemperaturaAlta) {
                            Log.d(TAG, "¡UMBRAL DE TEMPERATURA ALTA ALCANZADO! Valor: $temperature°C. Disparando notificación.")
                            showNotification(
                                TEMPERATURE_NOTIFICATION_ID,
                                "¡Alerta: Temperatura Alta!",
                                "Temperatura actual: ${temperature}°C. Considera ventilar.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Temperatura Alta!",
                                message = "Temperatura actual: ${temperature}°C. Considera ventilar.",
                                topic = subTopic,
                                payload = message.payload,
                                type = "Temperatura",
                                destinationRoute = "home"
                            )
                        } else if (temperature < umbralTemperaturaBaja) {
                            Log.d(TAG, "¡UMBRAL DE TEMPERATURA BAJA ALCANZADO! Valor: $temperature°C. Disparando notificación.")
                            showNotification(
                                TEMPERATURE_NOTIFICATION_ID,
                                "¡Alerta: Temperatura Baja!",
                                "Temperatura actual: ${temperature}°C. Podría hacer frío.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Temperatura Baja!",
                                message = "Temperatura actual: ${temperature}°C. Podría hacer frío.",
                                topic = subTopic,
                                payload = message.payload,
                                type = "Temperatura",
                                destinationRoute = "home"
                            )
                        } else {
                            Log.d(TAG, "Temperatura normal: $temperature°C. No se dispara notificación.")
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error al convertir temperatura a número: '${message.payload}'. Fin del procesamiento.", e)
                    }
                } else {
                    Log.w(TAG, "Payload de temperatura vacío o inválido. No se procesa.")
                }
            }
            "humidity" -> {
                Log.d(TAG, "SubTópico es 'humidity'.")
                if (message.payload.isNotBlank()) {
                    try {
                        val humidity = message.payload.toDouble()
                        val umbralHumedadAlta = 65.0
                        val umbralHumedadBaja = 40.0
                        Log.d(TAG, "Humedad convertida: ${humidity}%. Umbral Alto: $umbralHumedadAlta, Umbral Bajo: $umbralHumedadBaja")

                        if (humidity > umbralHumedadAlta) {
                            Log.d(TAG, "¡UMBRAL DE HUMEDAD ALTA ALCANZADO! Valor: ${humidity}%. Disparando notificación.")
                            showNotification(
                                HUMIDITY_NOTIFICATION_ID,
                                "¡Alerta: Humedad Alta!",
                                "Humedad actual: ${humidity}%. Considera ventilar o deshumidificar.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Humedad Alta!",
                                message = "Humedad actual: ${humidity}%. Considera ventilar o deshumidificar.",
                                topic = subTopic,
                                payload = message.payload,
                                type = "Humedad",
                                destinationRoute = "home"
                            )
                        } else if (humidity < umbralHumedadBaja) {
                            Log.d(TAG, "¡UMBRAL DE HUMEDAD BAJA ALCANZADO! Valor: ${humidity}%. Disparando notificación.")
                            showNotification(
                                HUMIDITY_NOTIFICATION_ID,
                                "¡Alerta: Humedad Baja!",
                                "Humedad actual: ${humidity}%. Podría ser necesario humidificar.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "!Alerta: Humedad Baja!",
                                message = "Humedad actual: ${humidity}%. Podría ser necesario humidificar.",
                                topic = subTopic,
                                payload = message.payload,
                                type = "Humedad",
                                destinationRoute = "home"
                            )
                        } else {
                            Log.d(TAG, "Humedad normal: ${humidity}%. No se dispara notificación.")
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error al convertir humedad a número: '${message.payload}'. Fin del procesamiento.", e)
                    }
                } else {
                    Log.w(TAG, "Payload de humedad vacío o inválido. No se procesa.")
                }
            }
            "air_quality" -> {
                Log.d(TAG, "SubTópico es 'air_quality'.")
                if (message.payload.isNotBlank()) {
                    try {
                        val airQuality = message.payload.toDouble()
                        val umbralCalidadAireMala = 1000.0
                        Log.d(TAG, "Calidad del aire convertida: $airQuality. Umbral Malo: $umbralCalidadAireMala")

                        if (airQuality > umbralCalidadAireMala) {
                            Log.d(TAG, "¡UMBRAL DE CALIDAD DEL AIRE BAJA ALCANZADO! Valor: $airQuality. Disparando notificación.")
                            showNotification(
                                AIR_QUALITY_NOTIFICATION_ID,
                                "¡Alerta: Calidad del Aire Baja!",
                                "Calidad del aire: ${airQuality}. Considera la ventilación.",
                                Routes.Home
                            )
                            saveNotificationToFirestore(
                                title = "Alerta: Calidad del Aire Baja",
                                message = "Calidad del aire: ${airQuality}. Considera la ventilación.",
                                topic = subTopic,
                                payload = message.payload,
                                type = "Calidad del Aire",
                                destinationRoute = "home"
                            )
                        } else {
                            Log.d(TAG, "Calidad del aire normal: ${airQuality}. No se dispara notificación.")
                        }
                    } catch (e: NumberFormatException) {
                        Log.e(TAG, "Error al convertir calidad del aire a número: '${message.payload}'. Fin del procesamiento.", e)
                    }
                } else {
                    Log.w(TAG, "Payload de calidad del aire vacío o inválido. No se procesa.")
                }
            }
            "feeding" -> {
                Log.d(TAG, "SubTópico es 'feeding'.")
                if (message.payload.isNotBlank()) {
                    Log.d(TAG, "Mensaje de alimentación recibido: '${message.payload}'. Disparando notificación informativa.")
                    showNotification(
                        FOOD_LEVEL_NOTIFICATION_ID,
                        "Información de Alimentación",
                        "Se ha registrado un evento de alimentación: ${message.payload}",
                        Routes.Feeding
                    )
                    saveNotificationToFirestore(
                        title = "Información de Alimentación",
                        message = "Se ha registrado un evento de alimentación: ${message.payload}",
                        topic = subTopic,
                        payload = message.payload,
                        type = "Alimentación",
                        destinationRoute = Routes.Feeding
                    )
                } else {
                    Log.w(TAG, "Payload de alimentación vacío o inválido. No se procesa.")
                }
            }
            "feeding/confirmacion" -> {
                Log.d(TAG, "SubTópico es 'feeding/confirmacion'.")
                if (message.payload.isNotBlank()) {
                    when (message.payload) {
                        "Horario de monitoreo actualizado" -> {
                            Log.d(TAG, "Payload de monitoreo recibido. Disparando notificación y guardando en Firestore.")
                            showNotification(
                                FOOD_LEVEL_NOTIFICATION_ID,
                                "Monitoreo",
                                message.payload,
                                Routes.Feeding
                            )
                            saveNotificationToFirestore(
                                title = "Monitoreo",
                                message = message.payload,
                                topic = subTopic,
                                payload = message.payload,
                                type = "Movimiento",
                                destinationRoute = Routes.Feeding
                            )
                        }
                        else -> {
                            Log.d(TAG, "Confirmación de alimentación recibida: '${message.payload}'. Disparando notificación.")
                            showNotification(
                                FOOD_LEVEL_NOTIFICATION_ID,
                                "Alimentación Confirmada",
                                "La alimentación ha sido confirmada: ${message.payload}",
                                Routes.Feeding
                            )
                            saveNotificationToFirestore(
                                title = "Alimentación Confirmada",
                                message = "La alimentación ha sido confirmada: ${message.payload}",
                                topic = subTopic,
                                payload = message.payload,
                                type = "Alimentación",
                                destinationRoute = Routes.Feeding
                            )
                        }
                    }
                } else {
                    Log.w(TAG, "Payload de confirmación de alimentación vacío o inválido. No se procesa.")
                }
            }
            "feeding/nivel" -> {
                Log.d(TAG, "SubTópico es 'feeding/nivel'. Payload: '${message.payload}'.")
                if (message.payload.isNotBlank()) {
                    if (message.payload.equals("Bajo", ignoreCase = true)) {
                        showNotification(
                            FOOD_LEVEL_NOTIFICATION_ID,
                            "¡Alerta: Nivel de Alimento Bajo!",
                            "El nivel de alimento de las gallinas está bajo. Por favor, revisa el dispensador.",
                            Routes.Feeding
                        )
                        saveNotificationToFirestore(
                            title = "Alerta: Nivel de Alimento Bajo",
                            message = "El nivel de alimento está bajo.",
                            topic = subTopic,
                            payload = message.payload,
                            type = "Alimento",
                            destinationRoute = Routes.Feeding
                        )
                    }
                }
            }
            else -> {
                Log.w(TAG, "Mensaje ignorado: SubTópico '$subTopic' no manejado por handleMqttMessage. Fin del procesamiento.")
            }
        }
        Log.d(TAG, "--- Fin procesamiento de mensaje MQTT ---")
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
                lightColor = getColor(R.color.purple_200)
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(fgChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun showNotification(id: Int, title: String, message: String, destinationRoute: String) {
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

        notificationManager.notify(id, notification)
        Log.d(TAG, "Notificación '$title' (ID: $id) enviada al sistema.")
    }

    private fun saveNotificationToFirestore(
        title: String,
        message: String,
        topic: String, // Este 'topic' ahora será el subTopic
        payload: String,
        type: String,
        destinationRoute: String
    ) {
        val userIdToSave = currentUserId
        val chickenCoopIdToSave = "defaultChickenCoop" // Esto se mantiene fijo ya que el ID no está en el tópico

        if (userIdToSave == null) {
            Log.e(TAG, "No se puede guardar la notificación en Firestore: userId es nulo.")
            return
        }

        val notificationRecord = NotificationRecord(
            title = title,
            description = message,
            type = type,
            topic = topic, // Aquí se guarda el subTopic
            payload = payload,
            destinationRoute = destinationRoute,
            isRead = false,
            userId = userIdToSave,
            chickenCoopId = chickenCoopIdToSave
        )

        val alertsCollectionRef = firestore.collection("users")
            .document(userIdToSave)
            .collection("chicken_coop")
            .document(chickenCoopIdToSave)
            .collection("alerts")

        alertsCollectionRef.add(notificationRecord)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Notificación guardada en Firestore con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al guardar notificación en Firestore: ${e.message}", e)
            }
    }
}