package com.modulap.nidosano.ui.screens.auth

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.components.StyledTextField
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.White
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.firestore
import com.modulap.nidosano.ui.navigation.Routes

// Importaciones adicionales para ClickableText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth: FirebaseAuth = Firebase.auth
    val context = LocalContext.current // Obtén el contexto

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Bienvenido",
                style = MaterialTheme.typography.headlineLarge,
                color = OrangePrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )


            Text(
                text = "Comienza a cuidar de tu gallinero de forma inteligente",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 50.dp)
            )


            // Correo
            StyledTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                placeholder = "Ingrese su correo electrónico",
                keyboardType = KeyboardType.Email
            )


            // Contraseña
            StyledTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                placeholder = "Ingrese una contraseña",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            // Botón
            ButtonPrimary(
                text = "Iniciar sesión",
                onClick = {
                    // Autenticación del usuario
                    FirebaseAuth.getInstance().setLanguageCode("es")
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Inicio de sesión exitoso
                                Log.d(TAG, "signInWithEmail:success")
                                val user = auth.currentUser
                                val email = FirebaseAuth.getInstance().currentUser?.email

                                if (email != null) {
                                    val db = Firebase.firestore
                                    db.collection("users")
                                        .whereEqualTo("email", email)
                                        .get()
                                        .addOnSuccessListener { documents ->
                                            if (!documents.isEmpty) {
                                                val document = documents.documents[0]
                                                val name = document.getString("name")
                                                Log.d("Firestore", "Usuario: $name")
                                            } else {
                                                Log.d("Firestore", "No se encontró un usuario con ese email")
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("Firestore", "Error al obtener el usuario: ${exception.message}")
                                        }
                                }

                                //Navegar a la siguiente pantalla
                                navController.navigate("home"){
                                    popUpTo(Routes.Home) { inclusive = true }
                                }
                                Toast.makeText(
                                    context,
                                    "Autenticación exitosa.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                // Si el inicio de sesión falla, muestra un mensaje al usuario.
                                Log.w(TAG, "signInWithEmail:failure", task.exception)
                                Toast.makeText(
                                    context, // Usa el contexto obtenido
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        }
                }
            )

            // Nuevo ClickableText para "Crear cuenta"
            // padding top for the space between the button and the text.
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextGray)) {
                    append("¿No tienes una cuenta? ")
                }
                // Attach a string annotation that stores a URL to the text "Crear cuenta".
                // This annotation will be used to identify the clickable part.
                pushStringAnnotation(tag = "CREATE_ACCOUNT", annotation = "create_account_route")
                withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold)) {
                    append("Crear cuenta")
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    val annotation = annotatedString.getStringAnnotations(tag = "CREATE_ACCOUNT", start = offset, end = offset)
                        .firstOrNull()
                    if (annotation != null) {
                        navController.navigate(Routes.Register)
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}


@Preview
@Composable
fun LoginPreview(){
    LoginScreen(navController = NavHostController(LocalContext.current))
}