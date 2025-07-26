package com.modulap.nidosano.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.firebase.getHourlyDataForDate
import com.modulap.nidosano.data.model.HourlyRecord
import kotlinx.coroutines.launch

class HourlyViewModel : ViewModel() {
    var state by mutableStateOf<List<HourlyRecord>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadHourlyData(userId: String, coopId: String, date: String) {
        viewModelScope.launch {
            isLoading = true
            state = getHourlyDataForDate(userId, coopId, date)
            isLoading = false
        }
    }
}
