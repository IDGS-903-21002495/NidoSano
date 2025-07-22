package com.modulap.nidosano.ui.screens.monitoring

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.modulap.nidosano.ui.components.BottomNavBar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.modulap.nidosano.R
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.components.SensorCard
import com.modulap.nidosano.ui.theme.OrangeSecondary
import com.modulap.nidosano.ui.theme.TextTitleOrange


@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MonitoringScreen(navController: NavHostController) {
    var currentRoute by remember { mutableStateOf("home") }
    val context = LocalContext.current

    var temperature by remember { mutableStateOf("–") }
    var humidity by remember { mutableStateOf("–") }
    var air_quality by remember { mutableStateOf("–") }
    var lighting_level by remember { mutableStateOf("–") }

    LaunchedEffect(Unit) {
        MQTTManagerHiveMQ.conectar { topic, value ->
            when (topic) {
                "temperature" -> temperature = "$value°C"
                "humidity" -> humidity = "$value%"
                "air_quality" -> air_quality = value
                "lighting_level" -> lighting_level = value
            }
        }
    }

    Scaffold(
        modifier = Modifier.background(Color.White),
        bottomBar = {
            BottomNavBar(currentRoute = currentRoute) { route ->
                currentRoute = route
                Log.d("BottomNav", "Seleccionado: $route")

            }
        }
    ) { paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Gallinero",
                style = MaterialTheme.typography.titleLarge,
                color = TextTitleOrange
            )

            // Estado general
            Text(
                text = "Todo en orden",
                style = MaterialTheme.typography.bodyLarge,
                color = OrangeSecondary
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Botón de historial
            ButtonPrimary(
                text = "Ver historial",
                onClick = {

                }
            )

            // Imagen del gallinero
            Image(
                painter = painterResource(id = R.drawable.granja),
                contentDescription = "Gallinero",
                modifier = Modifier.height(120.dp)
            )

            Spacer(modifier = Modifier.height(42.dp))

            // Tarjetas de sensores
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SensorCard("Temperatura", temperature, R.drawable.gallina)
                    SensorCard("Humedad", humidity, R.drawable.gallina)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    SensorCard("Calidad del aire", air_quality, R.drawable.granja)
                    SensorCard("Iluminación", lighting_level, R.drawable.granja)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Advertencia
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Actividad inusual detectada en el gallinero.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Text(
                        "Se detectó movimiento fuera del horario habitual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        "9:00AM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}
