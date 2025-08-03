package com.modulap.nidosano.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import java.nio.charset.StandardCharsets.UTF_8
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.util.UUID
import com.google.gson.Gson
import com.modulap.nidosano.data.model.FeedingSchedule // Importa tu modelo FeedingSchedule
import com.modulap.nidosano.data.model.MonitoringSchedule

// Representa un mensaje MQTT recibido
data class MqttMessage(val topic: String, val payload: String)

object MQTTManagerHiveMQ {

    private var mqttClient: Mqtt5AsyncClient? = null
    var currentUserId: String? = null

    const val GLOBAL_USER_ID_CONFIG_TOPIC = "nidosano/config/global_user_id"

    enum class ConnectionState { CONNECTING, CONNECTED, DISCONNECTED, ERROR }

    private val _messagesFlow = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 64)
    val messagesFlow: SharedFlow<MqttMessage> = _messagesFlow.asSharedFlow()

    private val _connectionStateFlow = MutableSharedFlow<Pair<ConnectionState, String?>>(extraBufferCapacity = 1)
    val connectionStateFlow: SharedFlow<Pair<ConnectionState, String?>> = _connectionStateFlow.asSharedFlow()

    // --- NUEVOS StateFlows para mantener los últimos niveles conocidos ---
    private val _lastKnownFoodLevel = MutableStateFlow<String>("–")
    val lastKnownFoodLevel: StateFlow<String> = _lastKnownFoodLevel.asStateFlow()

    private val _lastKnownWaterLevel = MutableStateFlow<String>("–")
    val lastKnownWaterLevel: StateFlow<String> = _lastKnownWaterLevel.asStateFlow()
    // -------------------------------------------------------------------

    // Base del tópico para todos los usuarios y el gallinero por defecto
    const val BASE_USER_TOPIC_PREFIX = "nidosano/defaultChickenCoop/users"

    // --- NUEVAS CONSTANTES DE TÓPICOS PARA LOS HORARIOS ---
    private const val FEEDING_SCHEDULE_BASE_SUB_TOPIC = "feeding_schedule"
    private const val FEEDING_SCHEDULE_CREATE_SUB_TOPIC = "$FEEDING_SCHEDULE_BASE_SUB_TOPIC/create"
    private const val FEEDING_SCHEDULE_UPDATE_SUB_TOPIC = "$FEEDING_SCHEDULE_BASE_SUB_TOPIC/update"
    private const val FEEDING_SCHEDULE_DELETE_SUB_TOPIC = "$FEEDING_SCHEDULE_BASE_SUB_TOPIC/delete"
    // -----------------------------------------------------

    // Instancia de Gson para serializar/deserializar JSON
    private val gson = Gson()

    @RequiresApi(Build.VERSION_CODES.N)
    fun conectar(userId: String) {
        // Si ya estamos conectados con el mismo usuario, no hacer nada
        if (mqttClient?.state?.isConnected == true && currentUserId == userId) {
            Log.d("MQTTManagerHiveMQ", "Cliente MQTT ya conectado para userId: $userId. Reutilizando conexión.")
            _connectionStateFlow.tryEmit(ConnectionState.CONNECTED to null)
            return
        }

        // Si el userId es diferente o no estamos conectados, desconectar y establecer nueva conexión
        if (mqttClient?.state?.isConnected == true && currentUserId != userId) {
            Log.d("MQTTManagerHiveMQ", "Conexión existente con diferente userId ($currentUserId). Desconectando para reconectar con $userId.")
            desconectar() // Desconectar para asegurar una conexión limpia con el nuevo usuario
        }

        currentUserId = userId
        Log.d("MQTTManagerHiveMQ", "Iniciando nueva conexión MQTT para userId: $userId...")
        _connectionStateFlow.tryEmit(ConnectionState.CONNECTING to "Estableciendo conexión...")

        val clientId = "NidoSanoApp_${userId}_${UUID.randomUUID().toString().substring(0, 8)}"

        mqttClient = MqttClient.builder()
            .useMqttVersion5()
            .identifier(clientId) // Usar un ID de cliente único para cada instancia de aplicación/usuario
            .serverHost("0c8ff25959a14816b3cfa2771b75e00a.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .buildAsync()

        mqttClient?.connectWith()
            ?.simpleAuth()
            ?.username("hivemq.webclient.1752384307171")
            ?.password(UTF_8.encode("<20,.1>3FHRZByGzagqf")) // Tu contraseña actual
            ?.applySimpleAuth()
            ?.send()
            ?.whenComplete { connAck, throwable ->
                if (throwable == null) {
                    _connectionStateFlow.tryEmit(ConnectionState.CONNECTED to "Conexión exitosa.")
                    Log.d("MQTTManagerHiveMQ", "Conexión MQTT establecida para userId: $userId.")

                    // Suscribirse al tópico comodín del usuario
                    subscribeToUserWildcardTopic(userId)

                    // Suscribirse también al tópico GLOBAL_USER_ID_CONFIG_TOPIC para reconfirmar (opcional)
                    subscribeToGlobalUserIdTopic()

                    // Configurar el listener de mensajes globalmente (se ejecutará solo una vez por cliente)
                    mqttClient?.publishes(MqttGlobalPublishFilter.ALL) { mensaje: Mqtt5Publish ->
                        val payloadBuffer = mensaje.payload.orElse(null)
                        val payload = if (payloadBuffer != null) {
                            val bytes = ByteArray(payloadBuffer.remaining())
                            payloadBuffer.get(bytes)
                            String(bytes, UTF_8)
                        } else {
                            "Sin datos"
                        }

                        val fullTopic = mensaje.topic.toString()
                        val emittedTopic: String

                        // Intentar extraer el subTopic si coincide con la estructura de usuario
                        val expectedUserPrefix = "$BASE_USER_TOPIC_PREFIX/$currentUserId/"
                        if (currentUserId != null && fullTopic.startsWith(expectedUserPrefix)) {
                            emittedTopic = fullTopic.substringAfter(expectedUserPrefix)
                            Log.d("MQTTManagerHiveMQ", "Mensaje recibido (subTopic): Tópico='${emittedTopic}', Payload='$payload'")

                            // --- Lógica para actualizar los nuevos StateFlows ---
                            when (emittedTopic) {
                                "feeding/nivel" -> {
                                    _lastKnownFoodLevel.value = payload
                                    Log.d("MQTTManagerHiveMQ", "Último Nivel de Alimento Actualizado: $payload")
                                }
                                "water/nivel" -> {
                                    _lastKnownWaterLevel.value = payload
                                    Log.d("MQTTManagerHiveMQ", "Último Nivel de Agua Actualizado: $payload")
                                }
                                // Puedes añadir lógica aquí para los tópicos de schedule si el backend envía confirmaciones
                                // Por ejemplo:
                                // FEEDING_SCHEDULE_CREATE_SUB_TOPIC -> { Log.d("MQTT", "Confirmación de creación de horario: $payload") }
                                // Y emitirlo al _messagesFlow si la UI lo necesita.
                            }
                            // --------------------------------------------------

                        } else {
                            // Si no es un tópico de usuario, enviar el tópico completo (ej. GLOBAL_USER_ID_CONFIG_TOPIC)
                            emittedTopic = fullTopic
                            Log.d("MQTTManagerHiveMQ", "Mensaje recibido (fullTopic): Tópico='${emittedTopic}', Payload='$payload'")
                        }

                        // Emitir el mensaje a través del SharedFlow con el tópico procesado
                        _messagesFlow.tryEmit(MqttMessage(emittedTopic, payload))
                    }
                } else {
                    _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error de conexión: ${throwable.message}")
                    Log.e("MQTTManagerHiveMQ", "Error de conexión MQTT para userId $userId: ${throwable.message}", throwable)
                }
            }
    }

    // Método de conexión sin userId (para cuando el servicio se inicie sin un usuario logueado)
    fun conectar() {
        if (currentUserId != null) {
            conectar(currentUserId!!)
        } else {
            Log.w("MQTTManagerHiveMQ", "Intentando conectar sin userId. La conexión se realizará solo cuando se proporcione un userId.")
            _connectionStateFlow.tryEmit(ConnectionState.DISCONNECTED to "Esperando ID de usuario para conectar.")
            mqttClient = MqttClient.builder()
                .useMqttVersion5()
                .identifier("NidoSanoApp_NoUser_${UUID.randomUUID().toString().substring(0, 8)}")
                .serverHost("0c8ff25959a14816b3cfa2771b75e00a.s1.eu.hivemq.cloud")
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildAsync()

            mqttClient?.connectWith()
                ?.simpleAuth()
                ?.username("hivemq.webclient.1752384307171")
                ?.password(UTF_8.encode("<20,.1>3FHRZByGzagqf"))
                ?.applySimpleAuth()
                ?.send()
                ?.whenComplete { connAck, throwable ->
                    if (throwable == null) {
                        Log.d("MQTTManagerHiveMQ", "Conexión MQTT establecida sin userId específico. Suscribiendo a tópico global.")
                        subscribeToGlobalUserIdTopic() // Suscribirse al tópico global para recibir el userId
                        _connectionStateFlow.tryEmit(ConnectionState.CONNECTED to "Conectado, esperando ID de usuario.")

                        // Configurar el listener de mensajes globalmente para este cliente sin userId
                        mqttClient?.publishes(MqttGlobalPublishFilter.ALL) { mensaje: Mqtt5Publish ->
                            val payloadBuffer = mensaje.payload.orElse(null)
                            val payload = if (payloadBuffer != null) {
                                val bytes = ByteArray(payloadBuffer.remaining())
                                payloadBuffer.get(bytes)
                                String(bytes, UTF_8)
                            } else {
                                "Sin datos"
                            }

                            val fullTopic = mensaje.topic.toString()
                            val emittedTopic: String

                            val expectedUserPrefix = "$BASE_USER_TOPIC_PREFIX/$currentUserId/" // currentUserId es null aquí
                            if (currentUserId != null && fullTopic.startsWith(expectedUserPrefix)) {
                                emittedTopic = fullTopic.substringAfter(expectedUserPrefix)
                                Log.d("MQTTManagerHiveMQ", "Mensaje recibido (subTopic): Tópico='${emittedTopic}', Payload='$payload'")

                                // --- Lógica para actualizar los nuevos StateFlows (incluso sin userId, si llegan mensajes) ---
                                when (emittedTopic) {
                                    "feeding/nivel" -> {
                                        _lastKnownFoodLevel.value = payload
                                        Log.d("MQTTManagerHiveMQ", "Último Nivel de Alimento Actualizado: $payload")
                                    }
                                    "water/nivel" -> {
                                        _lastKnownWaterLevel.value = payload
                                        Log.d("MQTTManagerHiveMQ", "Último Nivel de Agua Actualizado: $payload")
                                    }
                                }
                                // -----------------------------------------------------------------------------------------

                            } else {
                                emittedTopic = fullTopic
                                Log.d("MQTTManagerHiveMQ", "Mensaje recibido (fullTopic): Tópico='${emittedTopic}', Payload='$payload'")
                            }
                            _messagesFlow.tryEmit(MqttMessage(emittedTopic, payload))
                        }

                    } else {
                        _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error de conexión sin ID de usuario: ${throwable.message}")
                        Log.e("MQTTManagerHiveMQ", "Error de conexión MQTT sin userId: ${throwable.message}", throwable)
                    }
                }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    private fun subscribeToUserWildcardTopic(userId: String) {
        if (mqttClient?.state?.isConnected != true) {
            Log.e("MQTTManagerHiveMQ", "No se puede suscribir al tópico comodín de usuario: Cliente MQTT no conectado.")
            return
        }

        val userSpecificTopics = listOf(
            "temperature",
            "humidity",
            "air_quality",
            "lighting_level",
            "movement/alert",
            "feeding/confirmacion",
            "feeding/nivel",
            "water/nivel",
            "monitoring_schedule",
            FEEDING_SCHEDULE_CREATE_SUB_TOPIC,
            FEEDING_SCHEDULE_DELETE_SUB_TOPIC,
            FEEDING_SCHEDULE_UPDATE_SUB_TOPIC
            // -----------------------------------------------------------------------------
        )

        userSpecificTopics.forEach { subTopic ->
            val fullTopic = "$BASE_USER_TOPIC_PREFIX/$userId/$subTopic"
            Log.d("MQTTManagerHiveMQ", "Attempting to subscribe to: $fullTopic")
            mqttClient?.subscribeWith()
                ?.topicFilter(fullTopic)
                ?.qos(MqttQos.AT_LEAST_ONCE)
                ?.send()
                ?.whenComplete { _, subThrowable ->
                    if (subThrowable == null) {
                        Log.d("MQTTManagerHiveMQ", "Suscrito al tema específico del usuario: $fullTopic")
                    } else {
                        Log.e("MQTTManagerHiveMQ", "Error al suscribirse al tema $fullTopic: ${subThrowable.message}")
                        _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error al suscribirse a topics de usuario: ${subThrowable.message}")
                    }
                }
        }
    }

    private fun subscribeToGlobalUserIdTopic() {
        if (mqttClient?.state?.isConnected != true) {
            Log.e("MQTTManagerHiveMQ", "No se puede suscribir al tópico global: Cliente MQTT no conectado.")
            return
        }
        Log.d("MQTTManagerHiveMQ", "Attempting to subscribe to: $GLOBAL_USER_ID_CONFIG_TOPIC")
        mqttClient?.subscribeWith()
            ?.topicFilter(GLOBAL_USER_ID_CONFIG_TOPIC)
            ?.qos(MqttQos.AT_LEAST_ONCE)
            ?.send()
            ?.whenComplete { _, subThrowable ->
                if (subThrowable == null) {
                    Log.d("MQTTManagerHiveMQ", "Suscrito al tópico global de UserID: $GLOBAL_USER_ID_CONFIG_TOPIC")
                } else {
                    Log.e("MQTTManagerHiveMQ", "Error al suscribirse al tópico global de UserID: ${subThrowable.message}")
                    _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error al suscribirse a tópico global: ${subThrowable.message}")
                }
            }
    }


    fun publicar(topic: String, message: String, qos: MqttQos = MqttQos.AT_LEAST_ONCE, retain: Boolean = false) {
        if (mqttClient?.state?.isConnected != true) {
            Log.e("MQTTManagerHiveMQ", "No se puede publicar: Cliente MQTT no inicializado o no conectado. Tema: $topic")
            _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error al publicar: Cliente no conectado.")
            return
        }

        val finalTopic: String
        if (topic == GLOBAL_USER_ID_CONFIG_TOPIC) {
            finalTopic = topic
        } else {
            // Para todos los demás tópicos, usar el prefijo de usuario
            if (currentUserId != null) {
                finalTopic = "$BASE_USER_TOPIC_PREFIX/$currentUserId/$topic"
            } else {
                Log.e("MQTTManagerHiveMQ", "No se puede publicar en tópico de usuario: currentUserId es nulo. Tema: $topic")
                _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error al publicar: ID de usuario no disponible.")
                return
            }
        }

        val payloadBytes = message.toByteArray(UTF_8)
        val byteBuffer = ByteBuffer.wrap(payloadBytes)

        mqttClient?.publishWith()
            ?.topic(finalTopic)
            ?.qos(qos)
            ?.retain(retain)
            ?.payload(byteBuffer)
            ?.send()
            ?.whenComplete { _, throwable ->
                if (throwable == null) {
                    Log.d("MQTTManagerHiveMQ", "Mensaje publicado exitosamente: Tópico='$finalTopic', Payload='$message'")
                }
                else {
                    Log.e("MQTTManagerHiveMQ", "Error al publicar mensaje en '$finalTopic': ${throwable.message}", throwable)
                    _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error al publicar mensaje: ${throwable.message}")
                }
            }
    }

    fun desconectar() {
        if (mqttClient?.state?.isConnected == true) {
            mqttClient?.disconnect()
            Log.d("MQTTManagerHiveMQ", "Desconectado de MQTT.")
            _connectionStateFlow.tryEmit(ConnectionState.DISCONNECTED to "Desconectado.")
            currentUserId = null // Limpiar el userId al desconectar
        } else if (mqttClient != null) {
            _connectionStateFlow.tryEmit(ConnectionState.DISCONNECTED to "No estaba conectado, pero se intenta desconectar.")
        }
        mqttClient = null
    }

    // --- NUEVOS MÉTODOS PARA MANEJAR LOS HORARIOS DE ALIMENTACIÓN ---

    fun createFeedingSchedule(schedule: FeedingSchedule) {
        val json = gson.toJson(schedule)
        publicar(FEEDING_SCHEDULE_CREATE_SUB_TOPIC, json, MqttQos.AT_LEAST_ONCE)
        Log.d("MQTTManagerHiveMQ", "Publicado comando CREAR horario: $json en $FEEDING_SCHEDULE_CREATE_SUB_TOPIC")
    }

    fun updateFeedingSchedule(schedule: FeedingSchedule) {
        val json = gson.toJson(schedule)
        publicar(FEEDING_SCHEDULE_UPDATE_SUB_TOPIC, json, MqttQos.AT_LEAST_ONCE)
        Log.d("MQTTManagerHiveMQ", "Publicado comando ACTUALIZAR horario: $json en $FEEDING_SCHEDULE_UPDATE_SUB_TOPIC")
    }

    fun deleteFeedingSchedule(scheduleId: String) {
        val json = gson.toJson(mapOf("id" to scheduleId))
        publicar(FEEDING_SCHEDULE_DELETE_SUB_TOPIC, json, MqttQos.AT_LEAST_ONCE)
        Log.d("MQTTManagerHiveMQ", "Publicado comando ELIMINAR horario: $json en $FEEDING_SCHEDULE_DELETE_SUB_TOPIC")
    }

    fun updateMonitoringSchedule(schedule: MonitoringSchedule) {
        val json = gson.toJson(schedule)
        publicar("monitoring_schedule", json, MqttQos.AT_LEAST_ONCE)
        Log.d("MQTTManagerHiveMQ", "Publicado comando actualizar horario horario: $json en monitoring_schedule")
    }
}