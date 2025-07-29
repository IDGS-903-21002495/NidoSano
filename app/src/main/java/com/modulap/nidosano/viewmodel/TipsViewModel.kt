package com.modulap.nidosano.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modulap.nidosano.data.firebase.getGuide
import com.modulap.nidosano.data.model.Tip
import kotlinx.coroutines.launch

data class TipSatate(
    val tip : List<Tip> = emptyList(),
    val isLong: Boolean = false

)

class TipsViewModel: ViewModel(){
    var state by mutableStateOf(TipSatate())
        private set

    fun loadTip(){
        viewModelScope.launch {
            state = state.copy(isLong = true)
            val data = getGuide()
            println("Tips recibidos:")
            data.forEach { println(it) }
            state = state.copy(isLong = false, tip = data)
        }
    }
}



