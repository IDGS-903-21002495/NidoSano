package com.modulap.nidosano.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

@RequiresApi(Build.VERSION_CODES.N)
class SecurityViewModel(private val sharedMqttViewModel: SharedMqttViewModel) : ViewModel() {

    val movementDetected: StateFlow<Boolean> = sharedMqttViewModel.isMovementDetected
    val connectionStatus: StateFlow<MQTTManagerHiveMQ.ConnectionState> = sharedMqttViewModel.connectionStatus
    val mainDisplayMessage: StateFlow<String> = sharedMqttViewModel.movementAlertMessage

    init {
        Log.d("SecurityViewModel", "SecurityViewModel inicializado. Observando SharedMqttViewModel.")
    }
}