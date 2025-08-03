package com.modulap.nidosano.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.data.model.MonitoringSchedule
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.N)
class SecurityViewModel(private val sharedMqttViewModel: SharedMqttViewModel) : ViewModel() {

    val movementDetected: StateFlow<Boolean> = sharedMqttViewModel.isMovementDetected
    val connectionStatus: StateFlow<MQTTManagerHiveMQ.ConnectionState> = sharedMqttViewModel.connectionStatus
    val mainDisplayMessage: StateFlow<String> = sharedMqttViewModel.movementAlertMessage
    private val _saveScheduleStatus = MutableStateFlow<CommandStatus>(CommandStatus.Idle)
    val saveScheduleStatus: StateFlow<CommandStatus> = _saveScheduleStatus.asStateFlow()
    val monitoringScheduleUpdated: StateFlow<Boolean> = sharedMqttViewModel.monitoringScheduleUpdated

    init {
        Log.d("SecurityViewModel", "SecurityViewModel inicializado. Observando SharedMqttViewModel.")
    }

    fun saveMonitoringSchedule(
        startHour: Int,
        endHour: Int,
    ) {
        viewModelScope.launch {
            _saveScheduleStatus.value = CommandStatus.Loading
            val userId = MQTTManagerHiveMQ.currentUserId
            // Validaciones
            if (userId == null) {
                _saveScheduleStatus.value =
                    CommandStatus.Error("Usuario no autenticado. No se puede guardar el horario.")
                return@launch
            }
            val schedule = MonitoringSchedule(startHour, endHour)

            try {
                MQTTManagerHiveMQ.updateMonitoringSchedule(schedule)
                _saveScheduleStatus.value = CommandStatus.Success

            } catch (e: Exception) {
                _saveScheduleStatus.value =
                    CommandStatus.Error("Error general al guardar horario: ${e.localizedMessage ?: "Desconocido"}")
                Log.e("FeedingScheduleViewModel", "Error al guardar horario: ${e.message}", e)
            }
        }
    }

}