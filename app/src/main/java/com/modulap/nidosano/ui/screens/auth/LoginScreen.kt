package com.modulap.nidosano.ui.screens.auth

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.components.StyledTextField
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.White
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel() // Inyección del ViewModel
) {
    val context = LocalContext.current

    // Observar los estados del ViewModel
    val isLoading by authViewModel.isLoading.collectAsState()
    val loginSuccess by authViewModel.loginSuccess.collectAsState() // Observar el éxito del login
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.userIdFlow.collectAsState()

    // Estados para los campos de entrada
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Efecto para manejar el éxito del login
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            Toast.makeText(context, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show()

            currentUser?.let { userId ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    MQTTManagerHiveMQ.conectar(userId)
                }
            }

            // Navegar a la pantalla de inicio después del login exitoso
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true } // Limpia la pila para evitar regresar al login
            }
            authViewModel.clearLoginSuccess() // Consumir el evento para que no se dispare de nuevo
        }
    }

    // Efecto para manejar mensajes de error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            authViewModel.clearErrorMessage() // Consumir el error
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White) // Asegúrate de que el fondo sea blanco
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
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp, // Ajusta el tamaño si es necesario
                    fontWeight = FontWeight.Bold // Añade negrita
                ),
                color = OrangePrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Text(
                text = "Comienza a cuidar de tu gallinero de forma inteligente",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp // Ajusta el tamaño si es necesario
                ),
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
                    authViewModel.loginUser(email, password) // Llama a la función del ViewModel
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // Deshabilita el botón mientras se carga
            )

            // Indicador de carga
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            // Nuevo ClickableText para "Crear cuenta"
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextGray)) {
                    append("¿No tienes una cuenta? ")
                }
                pushStringAnnotation(tag = "CREATE_ACCOUNT", annotation = "create_account_route")
                withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold)) {
                    append("Crear cuenta")
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "CREATE_ACCOUNT", start = offset, end = offset)
                        .firstOrNull()?.let {
                            navController.navigate(Routes.Register) // Navega a la pantalla de registro
                        }
                },
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    LoginScreen(navController = rememberNavController())
}