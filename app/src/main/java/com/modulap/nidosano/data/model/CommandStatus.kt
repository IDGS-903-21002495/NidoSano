package com.modulap.nidosano.data.model

sealed class CommandStatus {
    object Idle : CommandStatus()
    object Loading : CommandStatus()
    object Success : CommandStatus()
    data class Error(val message: String) : CommandStatus()
}