package com.modulap.nidosano.ui.screens.profile

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.modulap.nidosano.NidoSanoApp
import com.modulap.nidosano.ui.viewmodel.AuthViewModel

@Composable
fun NidoSanoAppWrapper(startDestination: String?) {
    // Obtener la instancia del AuthViewModel
    val authViewModel: AuthViewModel = viewModel()
    val currentUserId by authViewModel.userIdFlow.collectAsState()

    // val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    // Pasar el userId a NidoSanoApp
    NidoSanoApp(startDestination = startDestination, userId = currentUserId)
}
