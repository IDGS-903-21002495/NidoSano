package com.modulap.nidosano.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await




class UserUpdateViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _updateError = MutableStateFlow<String?>(null)
    val updateError: StateFlow<String?> = _updateError

    var isEmailChanged = false
        private set

    fun updateUserData(
        userId: String,
        name: String,
        lastName: String,
        phoneNumber: String,
        email: String
    ) {
        _isUpdating.value = true
        _updateError.value = null
        _updateSuccess.value = false

        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                val currentEmail = currentUser?.email

                isEmailChanged = currentEmail != email && !email.isBlank()

                if (isEmailChanged && currentUser != null) {
                    currentUser.verifyBeforeUpdateEmail(email).await()
                    Log.d("UserUpdate", "Correo de verificación enviado a $email")
                }

                val updates = hashMapOf<String, Any>(
                    "name" to name,
                    "last_name" to lastName,
                    "phone_number" to phoneNumber,
                    "email" to email
                )

                firestore.collection("users").document(userId)
                    .update(updates)
                    .await()

                _updateSuccess.value = true
                Log.d("UserUpdate", "Datos actualizados correctamente")
            } catch (e: Exception) {
                _updateError.value = when {
                    e.message?.contains("requires recent authentication") == true -> {
                        "Por seguridad, inicia sesión nuevamente para cambiar tu correo"
                    }
                    e.message?.contains("email already in use") == true -> {
                        "Este correo ya está registrado"
                    }
                    else -> "Error al actualizar: ${e.localizedMessage}"
                }
                Log.e("UserUpdate", "Error al actualizar", e)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun clearUpdateState() {
        _updateSuccess.value = false
        _updateError.value = null
    }
}
