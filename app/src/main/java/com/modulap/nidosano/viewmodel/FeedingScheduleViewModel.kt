package com.modulap.nidosano.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.data.model.FeedingSchedule
import com.modulap.nidosano.data.firebase.FirestoreManager
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID // Para generar IDs únicos para nuevos horarios

class FeedingScheduleViewModel(application: Application) : AndroidViewModel(application) {

    // Estado de la operación de guardar (crear/actualizar) un horario
    private val _saveScheduleStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val saveScheduleStatus: StateFlow<CommandStatus> = _saveScheduleStatus.asStateFlow()

    // Estado de la operación de eliminar un horario
    private val _deleteScheduleStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val deleteScheduleStatus: StateFlow<CommandStatus> = _deleteScheduleStatus.asStateFlow()

    // Lista de todos los horarios de alimentación para ser observada por la UI
    private val _feedingSchedules = MutableStateFlow<List<FeedingSchedule>>(emptyList())
    val feedingSchedules: StateFlow<List<FeedingSchedule>> = _feedingSchedules.asStateFlow()

    init {
        loadFeedingSchedules()
    }

    fun loadFeedingSchedules() {
        viewModelScope.launch {
            val userId = MQTTManagerHiveMQ.currentUserId
            if (userId == null) {
                Log.w("FeedingScheduleViewModel", "No userId disponible para cargar horarios. Asegúrate de que MQTTManagerHiveMQ.currentUserId se establece al inicio de la sesión.")
                _feedingSchedules.value = emptyList()
                return@launch
            }
            try {
                _feedingSchedules.value = FirestoreManager.getFeedingSchedules(userId)
                Log.d("FeedingScheduleViewModel", "Horarios cargados: ${_feedingSchedules.value.size}")
            } catch (e: Exception) {
                Log.e("FeedingScheduleViewModel", "Error al cargar horarios: ${e.message}", e)
                _feedingSchedules.value = emptyList() // Establecer como vacío en caso de error
            }
        }
    }

    fun saveFeedingSchedule(
        id: String?, // Null para crear, ID para actualizar
        hour: Int,
        minute: Int,
        duration: Int,
        frequency: String
    ) {
        viewModelScope.launch {
            _saveScheduleStatus.value = CommandStatus.Loading
            val userId = MQTTManagerHiveMQ.currentUserId

            // Validaciones
            if (userId == null) {
                _saveScheduleStatus.value = CommandStatus.Error("Usuario no autenticado. No se puede guardar el horario.")
                return@launch
            }
            if (duration <= 0) {
                _saveScheduleStatus.value = CommandStatus.Error("La duración del alimento debe ser mayor a 0.")
                return@launch
            }

            val scheduleId = id ?: UUID.randomUUID().toString()
            val schedule = FeedingSchedule(scheduleId, hour, minute, duration, frequency)

            try {
                val firestoreSuccess = FirestoreManager.saveFeedingSchedule(userId, schedule)

                if (firestoreSuccess) {
                    if (id == null) {
                        MQTTManagerHiveMQ.createFeedingSchedule(schedule)
                    } else {
                        MQTTManagerHiveMQ.updateFeedingSchedule(schedule)
                    }
                    _saveScheduleStatus.value = CommandStatus.Success
                    loadFeedingSchedules()
                } else {
                    _saveScheduleStatus.value = CommandStatus.Error("Error al guardar en la base de datos (Firestore).")
                }
            } catch (e: Exception) {
                _saveScheduleStatus.value = CommandStatus.Error("Error general al guardar horario: ${e.localizedMessage ?: "Desconocido"}")
                Log.e("FeedingScheduleViewModel", "Error al guardar horario: ${e.message}", e)
            }
        }
    }

    fun deleteFeedingSchedule(scheduleId: String) {
        viewModelScope.launch {
            _deleteScheduleStatus.value = CommandStatus.Loading // Indicar que la operación está en curso
            val userId = MQTTManagerHiveMQ.currentUserId

            if (userId == null) {
                _deleteScheduleStatus.value = CommandStatus.Error("Usuario no autenticado. No se puede eliminar el horario.")
                return@launch
            }

            try {
                val firestoreSuccess = FirestoreManager.deleteFeedingSchedule(userId, scheduleId)

                if (firestoreSuccess) {
                    MQTTManagerHiveMQ.deleteFeedingSchedule(scheduleId)
                    _deleteScheduleStatus.value = CommandStatus.Success // Indicar éxito
                    loadFeedingSchedules()
                } else {
                    _deleteScheduleStatus.value = CommandStatus.Error("Error al eliminar de la base de datos (Firestore).")
                }
            } catch (e: Exception) {
                _deleteScheduleStatus.value = CommandStatus.Error("Error general al eliminar horario: ${e.localizedMessage ?: "Desconocido"}")
                Log.e("FeedingScheduleViewModel", "Error al eliminar horario: ${e.message}", e)
            }
        }
    }

    fun resetSaveScheduleStatus() {
        _saveScheduleStatus.value = CommandStatus.Idle
    }

    fun resetDeleteScheduleStatus() {
        _deleteScheduleStatus.value = CommandStatus.Idle
    }
}