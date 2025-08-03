package com.modulap.nidosano.ui.screens.auth

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modulap.nidosano.R
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.viewmodel.AuthViewModel

@Composable
fun CreateAccountScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    // Observar los estados del ViewModel
    val isLoading by authViewModel.isLoading.collectAsState()
    val registrationSuccess by authViewModel.registrationSuccess.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val currentUser by authViewModel.userIdFlow.collectAsState()

    // Estados para los campos de entrada
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    // Efecto para manejar el éxito del registro
    LaunchedEffect(registrationSuccess, currentUser) {
        if (registrationSuccess) {


            currentUser?.let { userId ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    MQTTManagerHiveMQ.conectar(userId)
                }
            }

            Toast.makeText(context, "Cuenta creada exitosamente.", Toast.LENGTH_SHORT).show()
            navController.navigate(Routes.Home) {
                popUpTo(Routes.Login) { inclusive = true }
            }
            authViewModel.clearRegistrationSuccess()
        }
    }

    // Efecto para manejar mensajes de error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            authViewModel.clearErrorMessage() // Consumir el error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.angulo_izquierdo),
                    contentDescription = "Volver atrás",
                    tint = TextGray,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Crear cuenta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextGray
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Contenido principal (campos de entrada y botón)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            StyledTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre",
                placeholder = "Ingrese su nombre"
            )

            StyledTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = "Apellidos",
                placeholder = "Ingrese sus apellidos"
            )

            StyledTextField(
                value = phone,
                onValueChange = { phone = it },
                label = "Teléfono",
                placeholder = "Ingrese su número telefónico",
                keyboardType = KeyboardType.Phone
            )

            StyledTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo electrónico",
                placeholder = "Ingrese su correo electrónico",
                keyboardType = KeyboardType.Email
            )

            StyledTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                placeholder = "Ingrese una contraseña",
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botón "Crear cuenta"
            ButtonPrimary(
                text = "Crear cuenta",
                onClick = {
                    authViewModel.registerUser(name, lastName, phone, email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            if(isLoading){
                CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto "¿Ya tienes una cuenta? Inicia sesión"
            val annotatedString = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextGray)) {
                    append("¿Ya tienes una cuenta? ")
                }
                pushStringAnnotation(tag = "LOGIN", annotation = "login_route")
                withStyle(style = SpanStyle(color = OrangePrimary, fontWeight = FontWeight.Bold)) {
                    append("Inicia sesión")
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                        .firstOrNull()?.let {
                            navController.navigate(Routes.Login) {
                                popUpTo(Routes.Register) { inclusive = true }
                            }
                        }
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateAccountScreen() {
    CreateAccountScreen(navController = rememberNavController())
}