package com.modulap.nidosano.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.R
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.ui.components.BottomNavBar
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.White
import com.modulap.nidosano.viewmodel.ChickenCoopViewModel

@Composable
fun FeedingScreen(
    navController: NavHostController,
    chickenCoopViewModel: ChickenCoopViewModel = viewModel()) {
    var currentRoute by remember { mutableStateOf("feeding") }
    val context = LocalContext.current

    // Observar estados del ChickenCoopViewModel
    val dispenseFoodStatus by chickenCoopViewModel.dispenseFoodCommandStatus.collectAsState()
    val lastConfirmationMessage by chickenCoopViewModel.lastConfirmationMessage.collectAsState()
    val currentFoodLevel by chickenCoopViewModel.currentFoodLevel.collectAsState()
    val currentWaterLevel by chickenCoopViewModel.currentWaterLevel.collectAsState()

    // Efecto para manejar el feedback del comando de dispensar alimento
    LaunchedEffect(dispenseFoodStatus) {
        when(dispenseFoodStatus) {
            CommandStatus.Loading -> {
                Toast.makeText(context, "Conectando con dispensador...", Toast.LENGTH_SHORT).show()
            }
            is CommandStatus.Error -> {
                Toast.makeText(context, (dispenseFoodStatus as CommandStatus.Error).message, Toast.LENGTH_SHORT).show()
            }
            CommandStatus.Success -> {
                Toast.makeText(context, "Alimento dispensado con éxito", Toast.LENGTH_SHORT).show()
                chickenCoopViewModel.resetDispenseFoodCommandStatus()
            }
            CommandStatus.Idle -> {}
        }
    }

    // Efecto para mostrar el último mensaje de confirmación
    LaunchedEffect(lastConfirmationMessage) {
        lastConfirmationMessage?.let { message ->
            Toast.makeText(context, "Confirmación: $message", Toast.LENGTH_SHORT).show()
            chickenCoopViewModel.clearLastConfirmationMessage() // Consumir el mensaje para que no se repita
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(currentRoute = currentRoute) { route ->
                currentRoute = route
                navController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        }
    )
    { paddingValues ->
        // Contenido de la pantalla de Alimentación
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alimentación",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextTitleOrange,
                    modifier = Modifier.padding(start = 16.dp)
                )

                // Spacer para empujar el icono de perfil a la derecha
                Spacer(modifier = Modifier.weight(1f))

                // Icono de perfil (derecha)
                IconButton(onClick = { navController.navigate("profile") }) {
                    Icon(
                        painter = painterResource(id = R.drawable.circulo_de_usuario),
                        contentDescription = "Perfil",
                        tint = TextGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Subtítulo
            Text(
                text = "No olvides alimentar a tus gallinas",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    color = TextGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            // Tarjetas de estado (Alimento y Agua)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ahora usamos el valor dinámico del ViewModel para el estado
                StatusCard(title = "Alimento", status = currentFoodLevel, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                // Ahora usamos el valor dinámico del ViewModel para el estado del agua
                StatusCard(title = "Agua", status = currentWaterLevel, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Imagen de la gallina
            Image(
                painter = painterResource(id = R.drawable.gan),
                contentDescription = "Gallina",
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Botones de acción
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ButtonPrimary(
                    text = "Dispensar ahora",
                    onClick = {
                        chickenCoopViewModel.dispenseFoodNow()
                    },
                    enabled = dispenseFoodStatus != CommandStatus.Loading
                )
                if (dispenseFoodStatus == CommandStatus.Loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                }
                ButtonPrimary(text = "Ver programaciones", onClick = { navController.navigate("schedule_list") })
            }
        }

    }
}

@Composable
fun StatusCard(title: String, status: String, modifier: Modifier = Modifier) {
    val statusColor = when (status.lowercase()) {
        "lleno", "alto" -> Color(0xFF2E6931)
        "medio" -> Color(0xFFC59507)
        "bajo", "vacío" -> Color(0xFF941212)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Muestra el estado dinámico con el color calculado
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = statusColor
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewFeedingScreen() {
    FeedingScreen(navController = rememberNavController())
}