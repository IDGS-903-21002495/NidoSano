package com.modulap.nidosano.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.components.StyledTextField
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.viewmodel.UserUpdateViewModel
import com.modulap.nidosano.viewmodel.UserViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    userId: String,
) {
    val userViewModel: UserViewModel = viewModel(key = userId)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val updateViewModel: UserUpdateViewModel = viewModel()
    val updateSuccess by updateViewModel.updateSuccess.collectAsState()
    val updateError by updateViewModel.updateError.collectAsState()
    val isUpdating by updateViewModel.isUpdating.collectAsState()

    val userUiState = userViewModel.state

    var name by remember { mutableStateOf("") }
    var surnames by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var originalEmail by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        userViewModel.clear() // Limpia el estado anterior
        userViewModel.loadUser(userId) // Inicia la carga del usuario
    }

    LaunchedEffect(userUiState.user) {
        userUiState.user?.let { user ->
            name = user.name
            surnames = user.last_name
            phoneNumber = user.phone_number
            email = user.email
            originalEmail = user.email
        }
    }

    LaunchedEffect(updateSuccess, updateError) {
        if (updateSuccess) {
            val message = if (email != originalEmail) {
                "Datos guardados. Por favor verifica tu nuevo correo electrónico."
            } else {
                "Datos actualizados correctamente"
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }

        updateError?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Long
                )
                updateViewModel.clearUpdateState()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.angulo_izquierdo),
                        contentDescription = "Volver",
                        tint = TextGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Editar perfil",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextGray
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            when {
                userUiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Campo de Nombre
                        StyledTextField(
                            label = "Nombre",
                            placeholder = "Nombre",
                            value = name,
                            onValueChange = { name = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Apellidos
                        StyledTextField(
                            label = "Apellidos",
                            placeholder = "Apellidos",
                            value = surnames,
                            onValueChange = { surnames = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Teléfono
                        StyledTextField(
                            label = "Teléfono",
                            placeholder = "Teléfono",
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            keyboardType = KeyboardType.Phone
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo de Email
                        StyledTextField(
                            label = "Correo electrónico",
                            placeholder = "Correo electrónico",
                            value = email,
                            onValueChange = { email = it },
                            keyboardType = KeyboardType.Email
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        // Botón Guardar
                        ButtonPrimary(
                            text = if (isUpdating) "Guardando..." else "Guardar",
                            onClick = {
                                if (name.isBlank() || surnames.isBlank() || phoneNumber.isBlank() || email.isBlank()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Por favor completa todos los campos",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@ButtonPrimary
                                }

                                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Ingresa un correo electrónico válido",
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                    return@ButtonPrimary
                                }

                                updateViewModel.updateUserData(
                                    userId = userId,
                                    name = name,
                                    lastName = surnames,
                                    phoneNumber = phoneNumber,
                                    email = email
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isUpdating
                        )
                    }
                }
            }
        }
    }
}
