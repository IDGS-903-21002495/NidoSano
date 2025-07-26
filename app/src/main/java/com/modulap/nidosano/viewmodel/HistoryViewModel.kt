package com.modulap.nidosano.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.firebase.getLastDailySummaries
import com.modulap.nidosano.data.model.DailySummary
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class SummaryState(
    val summaries: List<DailySummary> = emptyList(),
    val isLoading: Boolean = false
)

class HistoryViewModel : ViewModel() {
    var state by mutableStateOf(SummaryState())
        private set

    fun loadSummaries(userId: String, coopId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            val data = getLastDailySummaries(userId, coopId)
            state = state.copy(isLoading = false, summaries = data)
        }
    }
}
