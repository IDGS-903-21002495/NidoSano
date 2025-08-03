package com.modulap.nidosano.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ChickenCoopViewModel(application: Application) : AndroidViewModel(application) {
    // --- Tópicos MQTT (estos son los sub-tópicos que se añadirán al prefijo de usuario) ---
    private val COMMAND_FEEDING_SUB_TOPIC = "feeding"
    private val CONFIRMATION_FEEDING_SUB_TOPIC = "feeding/confirmacion"

    // Estado para el comando de dispensar alimento
    private val _dispenseFoodCommandStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val dispenseFoodCommandStatus: StateFlow<CommandStatus> = _dispenseFoodCommandStatus
    // Estado para el comando de programar alimentación
    private val _programFeedingCommandStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val programFeedingCommandStatus: StateFlow<CommandStatus> = _programFeedingCommandStatus

    // Último mensaje de confirmación recibido
    private val _lastConfirmationMessage = MutableStateFlow<String?>(null)
    val lastConfirmationMessage: StateFlow<String?> = _lastConfirmationMessage
    val currentFoodLevel: StateFlow<String> = MQTTManagerHiveMQ.lastKnownFoodLevel
    val currentWaterLevel: StateFlow<String> = MQTTManagerHiveMQ.lastKnownWaterLevel

    init {
        listenToMqttMessages()
    }

    private fun listenToMqttMessages() {
        viewModelScope.launch {
            MQTTManagerHiveMQ.messagesFlow.collectLatest { message ->
                Log.d("ChickenCoopViewModel", "Mensaje MQTT recibido: ${message.topic} - ${message.payload}")
                when (message.topic) {
                    CONFIRMATION_FEEDING_SUB_TOPIC -> {
                        _lastConfirmationMessage.value = message.payload
                        if (message.payload.contains("Alimento dispensado")) {
                            _dispenseFoodCommandStatus.value = CommandStatus.Success
                        } else if (message.payload.contains("Horario programado")) {
                            _programFeedingCommandStatus.value = CommandStatus.Success
                        }
                    }
                }
            }
        }
    }

    fun dispenseFoodNow() {
        _dispenseFoodCommandStatus.value = CommandStatus.Loading
        _lastConfirmationMessage.value = null
        viewModelScope.launch {
            try {
                if (MQTTManagerHiveMQ.currentUserId != null) { // Agregado para verificar si hay un userId
                    MQTTManagerHiveMQ.publicar(COMMAND_FEEDING_SUB_TOPIC, "Alimento", MqttQos.AT_LEAST_ONCE)
                } else {
                    Log.e("ChickenCoopViewModel", "No se puede dispensar alimento: currentUserId es nulo.")
                    _dispenseFoodCommandStatus.value = CommandStatus.Error("Usuario no autenticado para dispensar alimento.")
                }
            } catch (e: Exception) {
                _dispenseFoodCommandStatus.value = CommandStatus.Error("Error al enviar comando: ${e.localizedMessage}")
                Log.e("ChickenCoopViewModel", "Error al dispensar alimento: ${e.message}", e)
            }
        }
    }

    fun programFeeding(hour: Int, minute: Int) {
        _programFeedingCommandStatus.value = CommandStatus.Loading
        _lastConfirmationMessage.value = null

        // Validaciones básicas de la hora
        if (hour !in 0..23 || minute !in 0..59) {
            _programFeedingCommandStatus.value = CommandStatus.Error("Hora o minuto inválidos.")
            return
        }

        viewModelScope.launch {
            try {
                if (MQTTManagerHiveMQ.currentUserId != null) { // Agregado para verificar si hay un userId
                    // Formatear la hora como "HH:MM"
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val formattedTime = timeFormat.format(java.util.Date(0, 0, 0, hour, minute))

                    val message = "programar|$formattedTime"
                    MQTTManagerHiveMQ.publicar(COMMAND_FEEDING_SUB_TOPIC, message, MqttQos.AT_LEAST_ONCE)
                } else {
                    Log.e("ChickenCoopViewModel", "No se puede programar alimentación: currentUserId es nulo.")
                    _programFeedingCommandStatus.value = CommandStatus.Error("Usuario no autenticado para programar alimentación.")
                }
            } catch (e: Exception) {
                _programFeedingCommandStatus.value = CommandStatus.Error("Error al programar alimentación: ${e.localizedMessage}")
                Log.e("ChickenCoopViewModel", "Error al programar alimentación: ${e.message}", e)
            }
        }
    }

    fun resetDispenseFoodCommandStatus() {
        _dispenseFoodCommandStatus.value = CommandStatus.Idle
    }

    fun resetProgramFeedingCommandStatus() {
        _programFeedingCommandStatus.value = CommandStatus.Idle
    }

    fun clearLastConfirmationMessage() {
        _lastConfirmationMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("ChickenCoopViewModel", "ChickenCoopViewModel cleared. La conexión MQTT es gestionada por el servicio.")
    }
}