package com.modulap.nidosano.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.modulap.nidosano.data.model.Notification
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.firebase.getNotification
import kotlinx.coroutines.launch

data class NotificationState(
    val notification: List<Notification> = emptyList(),
    val isLoading: Boolean = false
)

class NotificationViewModel : ViewModel(){
    var state by mutableStateOf(NotificationState())
    private set

    fun loadNotifications(userId: String, coopId: String){
        viewModelScope.launch {
        state = state.copy(isLoading = true)
            val data = getNotification(userId, coopId)
            state = state.copy(isLoading = false, notification = data)
        }

    }
}