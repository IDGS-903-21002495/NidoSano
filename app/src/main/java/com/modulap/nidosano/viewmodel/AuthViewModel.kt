package com.modulap.nidosano.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.modulap.nidosano.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

const val PREFS_NAME = "NidoSanoPrefs"
const val KEY_USER_ID = "userId"
const val KEY_USER_NAME = "userName"
const val KEY_USER_LAST_NAME = "userLastName"

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

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _userIdFlow = MutableStateFlow<String?>(null)
    val userIdFlow: StateFlow<String?> = _userIdFlow.asStateFlow()

    // Nuevo StateFlow para los datos del usuario (nombre y apellido)
    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName.asStateFlow()

    private val _currentUserLastName = MutableStateFlow<String?>(null)
    val currentUserLastName: StateFlow<String?> = _currentUserLastName.asStateFlow()



    init {
        // Al iniciar el ViewModel, cargar el userId, nombre y apellido de SharedPreferences
        val storedUserId = getUserIdFromPrefs()
        _userIdFlow.value = storedUserId

        // Cargar nombre y apellido al iniciar
        _currentUserName.value = sharedPrefs.getString(KEY_USER_NAME, null)
        _currentUserLastName.value = sharedPrefs.getString(KEY_USER_LAST_NAME, null)


        if (auth.currentUser != null && storedUserId != null) {
            _isAuthenticated.value = true
            Log.d("AuthViewModel", "Usuario ya autenticado y userId en prefs: $storedUserId")
        } else {
            _isAuthenticated.value = false
            Log.d("AuthViewModel", "Usuario no autenticado o userId no encontrado en prefs.")
        }
    }

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

                    saveUserIdToPrefs(userId)
                    saveUserNameAndLastNameToPrefs(name, lastName)
                    _userIdFlow.value = userId
                    _currentUserName.value = name
                    _currentUserLastName.value = lastName
                    _registrationSuccess.value = true
                    _isAuthenticated.value = true
                    Log.d("AuthViewModel", "Usuario registrado y datos guardados exitosamente. userId: $userId")

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
                        saveUserIdToPrefs(userId)
                        saveUserNameAndLastNameToPrefs(user.name, user.last_name)
                        _userIdFlow.value = userId
                        _currentUserName.value = user.name
                        _currentUserLastName.value = user.last_name
                        _loginSuccess.value = true
                        _isAuthenticated.value = true
                        Log.d("AuthViewModel", "Usuario inició sesión y datos guardados exitosamente. userId: $userId")
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

    private fun saveUserIdToPrefs(userId: String) {
        sharedPrefs.edit().apply {
            putString(KEY_USER_ID, userId)
            apply()
        }
        Log.d("AuthViewModel", "User ID guardado en SharedPreferences: $userId")
    }

    private fun saveUserNameAndLastNameToPrefs(name: String?, lastName: String?) {
        sharedPrefs.edit().apply {
            putString(KEY_USER_NAME, name)
            putString(KEY_USER_LAST_NAME, lastName)
            apply()
        }
        Log.d("AuthViewModel", "Nombre '$name' y Apellido '$lastName' guardados en SharedPreferences.")
    }

    // Método ahora privado ya que el acceso externo es a través de userIdFlow
    private fun getUserIdFromPrefs(): String? {
        val userId = sharedPrefs.getString(KEY_USER_ID, null)
        Log.d("AuthViewModel", "Obteniendo User ID de SharedPreferences: $userId")
        return userId
    }

    fun getUserDataFromPrefs(): Map<String, String?> {
        return mapOf(
            KEY_USER_ID to sharedPrefs.getString(KEY_USER_ID, null),
            KEY_USER_NAME to sharedPrefs.getString(KEY_USER_NAME, null),
            KEY_USER_LAST_NAME to sharedPrefs.getString(KEY_USER_LAST_NAME, null)
        )
    }

    fun clearUserDataFromPrefs() {
        sharedPrefs.edit().clear().apply()
        _userIdFlow.value = null
        _currentUserName.value = null // Limpiar también los valores del nombre y apellido
        _currentUserLastName.value = null
        Log.d("AuthViewModel", "Datos de usuario eliminados de SharedPreferences.")
    }

    fun logoutUser() {
        auth.signOut()
        clearUserDataFromPrefs()
        _isAuthenticated.value = false
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

    fun updateUserNameAndLastName(userId: String, newName: String, newLastName: String) {
        _isLoading.value = true
        _errorMessage.value = null

        if (userId.isBlank()) {
            _errorMessage.value = "Error: User ID no válido."
            _isLoading.value = false
            return
        }
        if (newName.isBlank() || newLastName.isBlank()) {
            _errorMessage.value = "El nombre y el apellido no pueden estar vacíos."
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val userRef = firestore.collection("users").document(userId)
                userRef.update(
                    mapOf(
                        "name" to newName,
                        "last_name" to newLastName
                    )
                ).await()

                saveUserNameAndLastNameToPrefs(newName, newLastName) // Actualizar SharedPreferences
                _currentUserName.value = newName // Actualizar StateFlow
                _currentUserLastName.value = newLastName // Actualizar StateFlow

                Log.d("AuthViewModel", "Nombre y apellido actualizados en Firestore y SharedPreferences.")
                Toast.makeText(getApplication(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show() // Feedback visual

            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar el perfil: ${e.localizedMessage ?: "Desconocido"}"
                Log.e("AuthViewModel", "Error updating user name/last name: ${e.message}", e)
                Toast.makeText(getApplication(), "Error al actualizar perfil", Toast.LENGTH_SHORT).show() // Feedback visual
            } finally {
                _isLoading.value = false
            }
        }
    }
}