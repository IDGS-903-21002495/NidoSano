package com.modulap.nidosano.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswordUpdateViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun updatePassword(currentPassword: String, newPassword: String) {
        _errorMessage.value = null
        _successMessage.value = null
        _isLoading.value = true

        val currentUser = auth.currentUser

        if (currentUser == null) {
            _errorMessage.value = "No hay un usuario autenticado."
            _isLoading.value = false
            return
        }

        if (newPassword.length < 6) {
            _errorMessage.value = "La nueva contraseña debe tener al menos 6 caracteres."
            _isLoading.value = false
            return
        }

        val email = currentUser.email
        if (email == null) {
            _errorMessage.value = "No se encontró email del usuario."
            _isLoading.value = false
            return
        }

        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                currentUser.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    _isLoading.value = false
                    if (updateTask.isSuccessful) {
                        _successMessage.value = "Contraseña actualizada exitosamente."
                    } else {
                        val error = updateTask.exception?.localizedMessage ?: "Error desconocido"
                        _errorMessage.value = "Error al actualizar la contraseña: $error"
                    }
                }
            } else {
                _isLoading.value = false
                val error = reauthTask.exception?.localizedMessage ?: "Error desconocido"
                _errorMessage.value = "Reautenticación fallida: $error"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }
}
