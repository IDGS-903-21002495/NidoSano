package com.modulap.nidosano.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.modulap.nidosano.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val PREFS_NAME = "NidoSanoPrefs"
//const val KEY_USER_ID = "userId"
//const val KEY_USER_NAME = "userName"
//const val KEY_USER_LAST_NAME = "userLastName"
//const val KEY_CHICKEN_COOP_ID = "chickenCoopId"
//const val KEY_USER_EMAIL = "userEmail"

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val sharedPrefs: SharedPreferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun registerUser(
        name: String,
        lastName: String,
        phone: String,
        email: String,
        password: String
    ) {
        _errorMessage.value = null
        _registrationSuccess.value = false
        _isLoading.value = true

        if (name.isEmpty() || lastName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Por favor, completa todos los campos."
            _isLoading.value = false
            return
        }
        if (password.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                val userId = firebaseUser?.uid

                if (userId != null) {
                    val newUser = User(
                        name = name,
                        last_name = lastName,
                        phone_number = phone,
                        email = email,
                        status = true
                    )
                    firestore.collection("users").document(userId).set(newUser).await()

                    val initialChickenCoopId = "defaultChickenCoop"
                    val initialChickenCoopData = hashMapOf(
                        "name" to "Mi Primer Gallinero",
                        "location" to "Desconocida",
                        "creationDate" to FieldValue.serverTimestamp()
                    )
                    firestore.collection("users").document(userId)
                        .collection("chicken_coop").document(initialChickenCoopId)
                        .set(initialChickenCoopData).await()

                    //saveUserDataToPrefs(userId, name, lastName, email, initialChickenCoopId)

                    _registrationSuccess.value = true
                    Log.d("AuthViewModel", "Usuario registrado y datos guardados exitosamente.")

                } else {
                    _errorMessage.value = "Error interno: User ID es nulo después del registro."
                }
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException -> "El correo electrónico ya está registrado."
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil (mínimo 6 caracteres)."
                    else -> "Error al crear la cuenta: ${e.localizedMessage ?: "Desconocido"}"
                }
                Log.e("AuthViewModel", "Error en el registro: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loginUser(email: String, password: String) {
        _errorMessage.value = null
        _loginSuccess.value = false
        _isLoading.value = true

        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "Por favor, ingresa tu correo y contraseña."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                val userId = firebaseUser?.uid

                if (userId != null) {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    val user = userDoc.toObject(User::class.java)

                    if (user != null) {
                        val chickenCoopDoc = firestore.collection("users").document(userId)
                            .collection("chicken_coop").document("defaultChickenCoop").get().await()
                        val chickenCoopId = chickenCoopDoc.id

                        //saveUserDataToPrefs(userId, user.name, user.last_name, user.email, chickenCoopId)

                        _loginSuccess.value = true
                        Log.d("AuthViewModel", "Usuario inició sesión y datos guardados exitosamente.")
                    } else {
                        _errorMessage.value = "Error: No se encontraron datos de usuario en Firestore."
                        auth.signOut()
                    }
                } else {
                    _errorMessage.value = "Error interno: User ID es nulo después del inicio de sesión."
                }
            } catch (e: Exception) {
                _errorMessage.value = when (e) {
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException -> "No existe una cuenta con este correo electrónico."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta."
                    else -> "Error al iniciar sesión: ${e.localizedMessage ?: "Desconocido"}"
                }
                Log.e("AuthViewModel", "Error en el inicio de sesión: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /*
    private fun saveUserDataToPrefs(
        userId: String,
        userName: String,
        userLastName: String,
        userEmail: String,
        chickenCoopId: String
    ) {
        sharedPrefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USER_LAST_NAME, userLastName)
            putString(KEY_USER_EMAIL, userEmail)
            putString(KEY_CHICKEN_COOP_ID, chickenCoopId)
            apply()
        }
        Log.d("AuthViewModel", "Datos de usuario guardados en SharedPreferences: $userId, $userName $userLastName, $userEmail, $chickenCoopId")
    }


     */

    /*
    fun getUserDataFromPrefs(): Map<String, String?> {
        val userId = sharedPrefs.getString(KEY_USER_ID, null)
        val userName = sharedPrefs.getString(KEY_USER_NAME, null)
        val userLastName = sharedPrefs.getString(KEY_USER_LAST_NAME, null)
        val userEmail = sharedPrefs.getString(KEY_USER_EMAIL, null)
        val chickenCoopId = sharedPrefs.getString(KEY_CHICKEN_COOP_ID, null)
        Log.d("AuthViewModel", "Obteniendo datos de SharedPreferences: $userId, $userName $userLastName, $userEmail, $chickenCoopId")
        return mapOf(
            KEY_USER_ID to userId,
            KEY_USER_NAME to userName,
            KEY_USER_LAST_NAME to userLastName,
            KEY_USER_EMAIL to userEmail,
            KEY_CHICKEN_COOP_ID to chickenCoopId
        )
    }


     */
    fun clearUserDataFromPrefs() {
        sharedPrefs.edit().clear().apply()
        Log.d("AuthViewModel", "Datos de usuario eliminados de SharedPreferences.")
    }

    fun logoutUser() {
        auth.signOut()
        clearUserDataFromPrefs()
        Log.d("AuthViewModel", "Sesión cerrada y SharedPreferences limpiadas.")
        _loginSuccess.value = false
        _registrationSuccess.value = false
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearRegistrationSuccess() {
        _registrationSuccess.value = false
    }

    fun clearLoginSuccess() {
        _loginSuccess.value = false
    }
}