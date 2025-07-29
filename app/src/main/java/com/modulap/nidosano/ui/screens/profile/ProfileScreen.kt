package com.modulap.nidosano.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.R
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.White
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.viewmodel.AuthViewModel
//import com.modulap.nidosano.ui.viewmodel.KEY_USER_NAME
//


@Composable
fun ProfileScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel()
) {
    // Obtener los datos del usuario de SharedPreferences
    //val userData = authViewModel.getUserDataFromPrefs()

    //val userName = userData[KEY_USER_NAME] ?: "Usuario"
    //val userLastName = userData[KEY_USER_LAST_NAME] ?: ""
    //val displayName = if (userLastName.isNotBlank()) "$userName $userLastName" else userName // Combinar

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                text = "Perfil",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextGray
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.gan),
                contentDescription = "Foto de Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Mostrar nombre y apellido combinados
            Text(
                text = "Nombre de usuarios",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextGray
                )
            )
            Spacer(modifier = Modifier.height(32.dp))

            ProfileMenuItem(
                icon = Icons.Filled.Edit,
                text = "Editar perfil",
                onClick = { navController.navigate("edit_profile")}
            )
            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(
                icon = Icons.Filled.Notifications,
                text = "Notificaciones",
                onClick = { /* TODO: Navegar a pantalla de notificaciones */ }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(
                icon = Icons.Filled.Lock,
                text = "Cambiar contraseña",
                onClick = {navController.navigate("edit_password") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuItem(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                text = "Historial",
                onClick = { navController.navigate("history") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            ButtonPrimary(
                text = "Cerrar sesión",
                onClick = {
                    authViewModel.logoutUser()
                    MQTTManagerHiveMQ.desconectar()
                    navController.navigate(Routes.Login) {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDE8D4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = TextGray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextGray
                    )
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.angulo_derecho),
                contentDescription = "Ir a",
                tint = TextGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen(navController = rememberNavController())
}