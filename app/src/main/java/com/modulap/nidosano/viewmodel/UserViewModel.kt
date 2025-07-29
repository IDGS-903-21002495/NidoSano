package com.modulap.nidosano.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.modulap.nidosano.data.model.User
import kotlinx.coroutines.launch
import com.modulap.nidosano.data.firebase.getUser



data class UserState(
    val user: User? = null ,
    val isLoading: Boolean = false
)

class UserViewModel : ViewModel(){
    var state by mutableStateOf(UserState())
    private  set

    fun loadUser(userId: String){
        viewModelScope.launch {
            clear()
            state = state.copy(isLoading = true)
            val data = getUser(userId)
            state = state.copy(isLoading =  false, user = data )
        }
    }
    fun clear() {
        state = UserState() // estado inicial vacío
    }




}
