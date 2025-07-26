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

// Representa un mensaje MQTT recibido
data class MqttMessage(val topic: String, val payload: String)

object MQTTManagerHiveMQ {

    private lateinit var mqttClient: Mqtt5AsyncClient

    enum class ConnectionState { CONNECTING, CONNECTED, DISCONNECTED, ERROR }

    // Usamos un MutableSharedFlow para enviar mensajes a múltiples suscriptores
    private val _messagesFlow = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 1) // Buffer para el último mensaje
    val messagesFlow: SharedFlow<MqttMessage> = _messagesFlow.asSharedFlow()

    // Usamos un MutableSharedFlow para enviar estados de conexión
    private val _connectionStateFlow = MutableSharedFlow<Pair<ConnectionState, String?>>(extraBufferCapacity = 1)
    val connectionStateFlow: SharedFlow<Pair<ConnectionState, String?>> = _connectionStateFlow.asSharedFlow()


    @RequiresApi(Build.VERSION_CODES.N)
    fun conectar() {
        if (::mqttClient.isInitialized && mqttClient.state.isConnected) {
            Log.d("MQTTManagerHiveMQ", "Cliente MQTT ya conectado. Reutilizando conexión y re-emitiendo estado.")
            // Emitir el estado actual para los nuevos observadores
            _connectionStateFlow.tryEmit(ConnectionState.CONNECTED to null)
            return
        }

        Log.d("MQTTManagerHiveMQ", "Iniciando nueva conexión MQTT...")
        _connectionStateFlow.tryEmit(ConnectionState.CONNECTING to "Estableciendo conexión...")


        mqttClient = MqttClient.builder()
            .useMqttVersion5()
            .serverHost("0c8ff25959a14816b3cfa2771b75e00a.s1.eu.hivemq.cloud")
            .serverPort(8883)
            .sslWithDefaultConfig()
            .buildAsync()

        mqttClient.connectWith()
            .simpleAuth()
            .username("hivemq.webclient.1752384307171")
            .password(UTF_8.encode("<20,.1>3FHRZByGzagqf"))
            .applySimpleAuth()
            .send()
            .whenComplete { _, throwable ->
                if (throwable == null) {
                    _connectionStateFlow.tryEmit(ConnectionState.CONNECTED to "Conexión exitosa.")
                    Log.d("MQTTManagerHiveMQ", "Conexión MQTT establecida.")

                    val topics = listOf("temperature", "humidity", "air_quality", "lighting_level", "movement/alert")

                    topics.forEach { topic ->
                        mqttClient.subscribeWith()
                            .topicFilter(topic)
                            .qos(MqttQos.AT_LEAST_ONCE)
                            .send()
                            .whenComplete { _, subThrowable ->
                                if (subThrowable == null) {
                                    Log.d("MQTTManagerHiveMQ", "Suscrito al tema: $topic")
                                } else {
                                    Log.e("MQTTManagerHiveMQ", "Error al suscribirse al tema $topic: ${subThrowable.message}")
                                    _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error al suscribirse a $topic: ${subThrowable.message}")
                                }
                            }
                    }

                    mqttClient.publishes(MqttGlobalPublishFilter.ALL) { mensaje: Mqtt5Publish ->
                        val payload = mensaje.payload.map { buffer ->
                            val readOnlyCopy = buffer.asReadOnlyBuffer()
                            val bytes = ByteArray(readOnlyCopy.remaining())
                            readOnlyCopy.get(bytes)
                            String(bytes, UTF_8)
                        }.orElse("Sin datos")

                        Log.d("MQTTManagerHiveMQ", "Mensaje recibido: Tópico='${mensaje.topic}', Payload='$payload'")
                        // Emitir el mensaje a través del SharedFlow
                        _messagesFlow.tryEmit(MqttMessage(mensaje.topic.toString(), payload))
                    }
                } else {
                    _connectionStateFlow.tryEmit(ConnectionState.ERROR to "Error de conexión: ${throwable.message}")
                    Log.e("MQTTManagerHiveMQ", "Error de conexión MQTT: ${throwable.message}", throwable)
                }
            }
    }

    fun desconectar() {
        if (::mqttClient.isInitialized && mqttClient.state.isConnected) {
            mqttClient.disconnect()
            Log.d("MQTTManagerHiveMQ", "Desconectado de MQTT.")
            _connectionStateFlow.tryEmit(ConnectionState.DISCONNECTED to "Desconectado.")
        } else if (::mqttClient.isInitialized) {
            // Si no está conectado pero fue inicializado, aún podemos emitir el estado
            _connectionStateFlow.tryEmit(ConnectionState.DISCONNECTED to "No estaba conectado, pero se intenta desconectar.")
        }
    }
}