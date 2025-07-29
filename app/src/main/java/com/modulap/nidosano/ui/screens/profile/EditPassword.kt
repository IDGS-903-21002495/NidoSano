 package com.modulap.nidosano.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.components.StyledTextField
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.viewmodel.PasswordUpdateViewModel
import kotlinx.coroutines.launch

 @OptIn(ExperimentalMaterial3Api::class)
 @Composable
 fun EditPasswordScreen(
     navController: NavHostController,
     viewModel: PasswordUpdateViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
     onBackClick: () -> Unit = {}
 ) {
     val context = LocalContext.current
     val snackbarHostState = remember { SnackbarHostState() }
     val scope = rememberCoroutineScope()

     var currentPassword by remember { mutableStateOf("") }
     var newPassword by remember { mutableStateOf("") }
     var confirmPassword by remember { mutableStateOf("") }

     val isLoading by viewModel.isLoading.collectAsState()
     val successMessage by viewModel.successMessage.collectAsState()
     val errorMessage by viewModel.errorMessage.collectAsState()

     Scaffold(
         snackbarHost = { SnackbarHost(snackbarHostState) },
         topBar = {
             Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .background(Color.White)
                     .padding(horizontal = 16.dp, vertical = 12.dp),
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
                     text = "Cambiar contraseña",
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
         Column(
             modifier = Modifier
                 .fillMaxSize()
                 .padding(innerPadding)
                 .padding(horizontal = 32.dp),
             verticalArrangement = Arrangement.Top,
             horizontalAlignment = Alignment.CenterHorizontally
         ) {
             Spacer(modifier = Modifier.height(24.dp))

             StyledTextField(
                 label = "Contraseña actual",
                 placeholder = "Ingresa tu contraseña actual",
                 value = currentPassword,
                 onValueChange = { currentPassword = it },
                 isPassword = true
             )
             Spacer(modifier = Modifier.height(16.dp))

             StyledTextField(
                 label = "Nueva contraseña",
                 placeholder = "Ingresa tu nueva contraseña",
                 value = newPassword,
                 onValueChange = { newPassword = it },
                 isPassword = true
             )
             Spacer(modifier = Modifier.height(16.dp))

             StyledTextField(
                 label = "Confirmar contraseña",
                 placeholder = "Confirma tu nueva contraseña",
                 value = confirmPassword,
                 onValueChange = { confirmPassword = it },
                 isPassword = true
             )
             Spacer(modifier = Modifier.height(32.dp))

             ButtonPrimary(
                 text = if (isLoading) "Cambiando..." else "Cambiar contraseña",
                 onClick = {
                     if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                         scope.launch {
                             snackbarHostState.showSnackbar(
                                 message = "Por favor completa todos los campos",
                                 duration = SnackbarDuration.Short
                             )
                         }
                         return@ButtonPrimary
                     }

                     if (newPassword != confirmPassword) {
                         scope.launch {
                             snackbarHostState.showSnackbar(
                                 message = "Las contraseñas no coinciden",
                                 duration = SnackbarDuration.Short
                             )
                         }
                         return@ButtonPrimary
                     }

                     if (newPassword.length < 6) {
                         scope.launch {
                             snackbarHostState.showSnackbar(
                                 message = "La contraseña debe tener al menos 6 caracteres",
                                 duration = SnackbarDuration.Short
                             )
                         }
                         return@ButtonPrimary
                     }

                     viewModel.updatePassword(currentPassword, newPassword)
                 },
                 modifier = Modifier.fillMaxWidth(),
                 enabled = !isLoading
             )
         }
     }

     LaunchedEffect(successMessage) {
         successMessage?.let {
             Toast.makeText(context, it, Toast.LENGTH_LONG).show()
             navController.popBackStack()
             viewModel.clearMessages()
         }
     }

     LaunchedEffect(errorMessage) {
         errorMessage?.let {
             scope.launch {
                 snackbarHostState.showSnackbar(it)
             }
             viewModel.clearMessages()
         }
     }
 }
