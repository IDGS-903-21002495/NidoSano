package com.modulap.nidosano.ui.screens.notification

import androidx.compose.foundation.Image
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop

import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Para R.drawable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.modulap.nidosano.R
import com.modulap.nidosano.data.model.Notification // Asegúrate de que esta importación sea correcta
import com.modulap.nidosano.ui.components.BottomNavBar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Eco
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.AlertGreenBg // Para alertas de éxito
import com.modulap.nidosano.ui.theme.AlertRedBg // Para alertas de error/críticas
import com.modulap.nidosano.ui.theme.AlertYellowBg
import com.modulap.nidosano.ui.theme.AlertNeutralBg
import com.modulap.nidosano.viewmodel.NotificationViewModel

// Importar CircularProgressIndicator
import androidx.compose.material3.CircularProgressIndicator


@Composable
fun NotificationScreen(
    navController: NavHostController,
    userId: String,
    coopId: String,
    viewModel: NotificationViewModel = viewModel() // Mantén la inyección de dependencia del ViewModel
) {
    var currentRoute by remember { mutableStateOf("notification") }

    // Observar el 'state' directamente desde tu NotificationViewModel
    val uiState = viewModel.state // Esto observará los cambios en 'state' gracias a 'by mutableStateOf'


    LaunchedEffect(Unit) {
        // Cargar las notificaciones cuando el Composable se inicializa
        viewModel.loadNotifications(userId, coopId)
    }

    Scaffold(
        modifier = Modifier.background(Color.White),
        bottomBar = {
            BottomNavBar(currentRoute = currentRoute) { route ->
                currentRoute = route
                navController.navigate(route) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(route) { inclusive = true }
                }
            }
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Historial de notificaciones",
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

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    // Muestra el indicador de carga en el centro mientras se obtienen los datos
                    Box(
                        modifier = Modifier.fillMaxSize(), // Ocupa todo el espacio restante
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                uiState.notification.isEmpty() -> {
                    // Muestra un mensaje si no hay notificaciones y no está cargando ni hay error
                    Text(
                        text = "No hay notificaciones para mostrar.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = TextGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    // Muestra la lista de notificaciones si hay datos y no está cargando
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.notification) { notification ->
                            AlertCard(notification = notification)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlertCard(notification: Notification) {
    val (backgroundColor, iconColor, iconPainter) = when (notification.type.uppercase().trim()) {
        "HUMEDAD" -> Triple(AlertYellowBg, MaterialTheme.colorScheme.primary, rememberVectorPainter(Icons.Filled.InvertColors))
        "MOVIMIENTO" -> Triple(AlertYellowBg, MaterialTheme.colorScheme.primary, rememberVectorPainter(Icons.Filled.DirectionsRun))
        "TEMPERATURA" -> Triple(AlertRedBg, MaterialTheme.colorScheme.error, rememberVectorPainter(Icons.Filled.Thermostat))
        "CALIDAD DEL AIRE" -> Triple(AlertGreenBg, MaterialTheme.colorScheme.secondary, rememberVectorPainter(Icons.Filled.Air))

        "ALIMENTACIÓN" -> Triple(AlertGreenBg, MaterialTheme.colorScheme.secondary, rememberVectorPainter(Icons.Filled.WaterDrop))
        else -> Triple(AlertNeutralBg, MaterialTheme.colorScheme.onSurfaceVariant, rememberVectorPainter(Icons.Filled.Info))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)  // Tamaño del fondo circular (icono + padding)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1
                    )

                    if (notification.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = notification.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 3
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = notification.time,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }
    }
}
