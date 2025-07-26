package com.modulap.nidosano.ui.screens.notification
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AccountCircle
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
import androidx.navigation.NavHostController
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.components.BottomNavBar

import com.modulap.nidosano.ui.theme.TextGray // Gris para el texto general
import com.modulap.nidosano.ui.theme.TextTitleOrange // Naranja para el título
import com.modulap.nidosano.ui.theme.AlertGreenBg // Para alertas de éxito
import com.modulap.nidosano.ui.theme.AlertRedBg // Para alertas de error/críticas
import com.modulap.nidosano.ui.theme.AlertYellowBg // Para alertas de advertencia (similar a BackgroundCream/OrangeLight en otras pantallas)
import com.modulap.nidosano.ui.theme.AlertNeutralBg // Para alertas generales/informativas (fondo de tarjeta ligero)


// Modelo de datos para una Alerta
data class Alert(
    val type: AlertType,
    val title: String,
    val description: String?,
    val time: String
)

enum class AlertType {
    OPTIMAL,      // Verde: Ambiente óptimo
    CRITICAL,     // Rojo: Condiciones críticas
    UNUSUAL,      // Gris/Blanco: Actividad inusual
    UPDATED,      // Gris/Blanco: Datos del cuidador actualizados
    WARNING       // Naranja: Alto nivel de humedad
}

@Composable
fun NotificationScreen(navController: NavHostController) {
    var currentRoute by remember { mutableStateOf("notification") }
    val context = LocalContext.current

    // Datos de ejemplo
    val alerts = listOf(
        Alert(AlertType.OPTIMAL, "El ambiente es óptimo para las gallinas.", null, "10:00AM"),
        Alert(AlertType.CRITICAL, "¡Condiciones críticas detectadas en el gallinero!", null, "9:30AM"),
        Alert(AlertType.UNUSUAL, "Actividad inusual detectada en el gallinero.", "Se detectó movimiento fuera del horario habitual.", "9:00AM"),
        Alert(AlertType.UPDATED, "Datos del cuidador actualizados", "La información del perfil se ha guardado correctamente. Actualización de contraseña realizada.", "9:00AM"),
        Alert(AlertType.WARNING, "¡Atención! Alto nivel de humedad en el ambiente.", null, "9:00AM")
    )

    Scaffold(
        modifier = Modifier.background(Color.White),
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
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = "Perfil",
                        tint = TextGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Alertas (LazyColumn para rendimiento)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(alerts) { alert ->
                    AlertCard(alert = alert)
                }
            }
        }
    }
}

@Composable
fun AlertCard(alert: Alert) {
    val (backgroundColor, iconColor, iconPainter) = when (alert.type) {
        AlertType.OPTIMAL -> Triple(AlertGreenBg, TextGray, painterResource(id = R.drawable.campana)) // Shield check for optimal
        AlertType.CRITICAL -> Triple(AlertRedBg, TextGray, rememberVectorPainter(Icons.Filled.Warning)) // Warning for critical
        AlertType.UNUSUAL -> Triple(AlertNeutralBg, TextGray, rememberVectorPainter(Icons.Filled.Info)) // Info for unusual
        AlertType.UPDATED -> Triple(AlertNeutralBg, TextGray, rememberVectorPainter(Icons.Filled.Info)) // Info for updated
        AlertType.WARNING -> Triple(AlertYellowBg, TextGray, rememberVectorPainter(Icons.Filled.Warning)) // Warning for warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (alert.description != null) 120.dp else 80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    alert.description?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                color = TextGray
                            )
                        )
                    }
                }
            }
            Text(
                text = alert.time,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    color = TextGray // Color para la hora
                ),
                modifier = Modifier.padding(start = 8.dp),
                maxLines = 1
            )
        }
    }
}
