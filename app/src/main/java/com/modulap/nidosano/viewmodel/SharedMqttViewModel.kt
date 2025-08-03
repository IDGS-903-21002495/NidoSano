package com.modulap.nidosano.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.firebase.FirestoreManager
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.data.model.FeedingSchedule
import com.modulap.nidosano.data.model.MonitoringSchedule
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.data.repository.MqttMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.N)
class SharedMqttViewModel : ViewModel() {

    // --- Estado de Conexión ---
    private val _connectionStatus = MutableStateFlow(MQTTManagerHiveMQ.ConnectionState.DISCONNECTED)
    val connectionStatus: StateFlow<MQTTManagerHiveMQ.ConnectionState> = _connectionStatus.asStateFlow()

    private val _connectionMessage = MutableStateFlow<String?>("Esperando conexión...")
    val connectionMessage: StateFlow<String?> = _connectionMessage.asStateFlow()

    private val _temperature = MutableStateFlow("–")
    val temperature: StateFlow<String> = _temperature.asStateFlow()

    private val _humidity = MutableStateFlow("–")
    val humidity: StateFlow<String> = _humidity.asStateFlow()

    private val _airQuality = MutableStateFlow("–")
    val airQuality: StateFlow<String> = _airQuality.asStateFlow()

    private val _lightingLevel = MutableStateFlow("–")
    val lightingLevel: StateFlow<String> = _lightingLevel.asStateFlow()

    private val _movementAlertMessage = MutableStateFlow("Sin detección reciente.")
    val movementAlertMessage: StateFlow<String> = _movementAlertMessage.asStateFlow()

    private val _monitoringScheduleUpdated = MutableStateFlow(false)
    val monitoringScheduleUpdated: StateFlow<Boolean> = _monitoringScheduleUpdated.asStateFlow()

    private val _isMovementDetected = MutableStateFlow(false)
    val isMovementDetected: StateFlow<Boolean> = _isMovementDetected.asStateFlow()

    private val _feedingStatusMessage = MutableStateFlow("Esperando información de alimentación.")
    val feedingStatusMessage: StateFlow<String> = _feedingStatusMessage.asStateFlow()

    init {
        Log.d("SharedMqttViewModel", "SharedMqttViewModel inicializado. Recolectando flujos MQTT.")
        viewModelScope.launch(Dispatchers.Main) {
            MQTTManagerHiveMQ.connectionStateFlow.collectLatest { (state, message) ->
                _connectionStatus.value = state
                _connectionMessage.value = message
                Log.d("SharedMqttViewModel", "Estado de conexión actualizado: $state, $message")
                if (state == MQTTManagerHiveMQ.ConnectionState.DISCONNECTED || state == MQTTManagerHiveMQ.ConnectionState.ERROR) {
                    _isMovementDetected.value = false
                    _movementAlertMessage.value = "Conexión perdida o error."
                    _temperature.value = "–"
                    _humidity.value = "–"
                    _airQuality.value = "–"
                    _lightingLevel.value = "–"
                    _feedingStatusMessage.value = "Conexión perdida, información de alimentación no disponible."
                }
            }
        }

        // Recolectar los mensajes MQTT
        viewModelScope.launch(Dispatchers.Main) {
            MQTTManagerHiveMQ.messagesFlow.collectLatest { mqttMessage ->
                Log.d("SharedMqttViewModel", "Mensaje recibido en SharedMqttViewModel: Tópico='${mqttMessage.topic}', Payload='${mqttMessage.payload}'")
                when (mqttMessage.topic) {
                    "temperature" -> _temperature.value = "${mqttMessage.payload}°C"
                    "humidity" -> _humidity.value = "${mqttMessage.payload}%"
                    "air_quality" -> _airQuality.value = mqttMessage.payload
                    "lighting_level" -> _lightingLevel.value = mqttMessage.payload
                    "movement/alert" -> {
                        _movementAlertMessage.value = mqttMessage.payload
                        _isMovementDetected.value = (mqttMessage.payload.trim() != "Movimiento detectado en: 0")
                    }
                    "feeding" -> {
                        _feedingStatusMessage.value = "Alimentación: ${mqttMessage.payload}"
                    }
                    "feeding/confirmacion" -> {
                        _feedingStatusMessage.value = "Alimentación Confirmada: ${mqttMessage.payload}"
                    }
                    else -> {
                        Log.w("SharedMqttViewModel", "Tópico no reconocido o no manejado para UI: ${mqttMessage.topic}")
                    }
                }
            }
        }
    }



    override fun onCleared() {
        super.onCleared()
        Log.d("SharedMqttViewModel", "SharedMqttViewModel cleared. La conexión MQTT es gestionada por el servicio.")
    }
}