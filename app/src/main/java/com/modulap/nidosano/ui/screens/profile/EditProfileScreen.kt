package com.modulap.nidosano.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.modulap.nidosano.ui.components.StyledTextField // <-- ¡Importa tu StyledTextField!
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.White
import com.modulap.nidosano.ui.viewmodel.AuthViewModel

@Composable
fun EditProfileScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    // Estados para los campos de texto
    var name by remember { mutableStateOf("Alicia") } // Valor inicial de la imagen
    var surnames by remember { mutableStateOf("Vázquez Fuentes") } // Valor inicial de la imagen
    var phoneNumber by remember { mutableStateOf("477009988") } // Valor inicial de la imagen
    var email by remember { mutableStateOf("alicia@gmail.com") } // Valor inicial de la imagen

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = "Editar perfil", // Título de la pantalla
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextGray
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Contenido principal de edición de perfil
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .weight(1f), // Para que el botón de guardar se vaya al fondo
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp)) // Espacio superior

            // Campo de Nombre
            StyledTextField(
                label = "Nombre",
                placeholder = "Alicia",
                value = name,
                onValueChange = { name = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Apellidos
            StyledTextField(
                label = "Apellidos",
                placeholder = "Vázquez Fuentes",
                value = surnames,
                onValueChange = { surnames = it }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Teléfono
            StyledTextField(
                label = "Teléfono",
                placeholder = "477009988",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                keyboardType = KeyboardType.Phone
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Correo electrónico
            StyledTextField(
                label = "Correo electrónico",
                placeholder = "alicia@gmail.com",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(32.dp)) // Espacio antes del botón

            ButtonPrimary(
                text = "Guardar",
                onClick = {
                    // TODO: Implementar lógica para guardar los cambios
                    navController.popBackStack() // Volver a la pantalla anterior (Perfil)
                },
                modifier = Modifier.fillMaxWidth() // Usa el modifier aquí
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewEditProfileScreen() {
    EditProfileScreen(navController = rememberNavController())
}