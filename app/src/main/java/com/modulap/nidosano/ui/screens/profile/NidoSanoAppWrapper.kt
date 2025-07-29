package com.modulap.nidosano.ui.screens.profile

import androidx.compose.runtime.*
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.modulap.nidosano.NidoSanoApp

@Composable
fun NidoSanoAppWrapper(startDestination: String?) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)

        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    NidoSanoApp(startDestination = startDestination, userId = currentUser?.uid)
}
