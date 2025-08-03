package com.modulap.nidosano.ui.screens.monitoring

import SensorCard
import android.annotation.SuppressLint
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modulap.nidosano.R
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.viewmodel.SharedMqttViewModel

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun MonitoringScreen(
    navController: NavHostController,
    viewModel: SharedMqttViewModel = viewModel()
) {
    var currentRoute by remember { mutableStateOf("home") }
    val context = LocalContext.current

    val temperature by viewModel.temperature.collectAsState()
    val humidity by viewModel.humidity.collectAsState()
    val airQuality by viewModel.airQuality.collectAsState()
    val lightingLevel by viewModel.lightingLevel.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val generalStatusMessage = remember(connectionStatus) {
        Log.d("MonitoringScreen", "Calculating generalStatusMessage for status: $connectionStatus")
        when (connectionStatus) {
            MQTTManagerHiveMQ.ConnectionState.CONNECTED -> "Conectado. Todo en orden."
            MQTTManagerHiveMQ.ConnectionState.CONNECTING -> "Conectando al gallinero..."
            MQTTManagerHiveMQ.ConnectionState.DISCONNECTED -> "Desconectado del gallinero."
            MQTTManagerHiveMQ.ConnectionState.ERROR -> "Error de conexión al gallinero."
        }
    }

    val generalStatusColor = remember(connectionStatus) {
        when (connectionStatus) {
            MQTTManagerHiveMQ.ConnectionState.CONNECTED -> OrangePrimary
            MQTTManagerHiveMQ.ConnectionState.CONNECTING -> Color(0xFFFFA500) // Naranja para conectando (opcional)
            MQTTManagerHiveMQ.ConnectionState.DISCONNECTED -> Color.Red
            MQTTManagerHiveMQ.ConnectionState.ERROR -> Color.Red
        }
    }

    LaunchedEffect(connectionStatus, generalStatusMessage) {
        Log.d("MonitoringScreen", "LaunchedEffect: connectionStatus changed to $connectionStatus. Displayed message: $generalStatusMessage")
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
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
                    text = "Gallinero",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextTitleOrange,
                    modifier = Modifier.padding(start = 16.dp)
                )

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

            Text(
                text = generalStatusMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = generalStatusColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                textAlign = Alignment.CenterHorizontally.toString().let { TextAlign.Center }
            )

            // Contenido principal de la pantalla de monitoreo
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    ButtonPrimary(
                        text = "Ver historial",
                        onClick = {
                            navController.navigate("history")
                        },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(42.dp))

                Image(
                    painter = painterResource(id = R.drawable.gan2),
                    contentDescription = "Gallinero",
                    modifier = Modifier
                        .height(160.dp)
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(42.dp))

                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SensorCard("Temperatura", temperature, R.drawable.temperatura_alta)
                        SensorCard("Humedad", humidity, R.drawable.punto_de_rocio)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SensorCard("Calidad del aire", airQuality, R.drawable.calor)
                        SensorCard("Iluminación", lightingLevel, R.drawable.eclipse)
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.N)
@Preview(showBackground = true)
@Composable
fun MonitoringScreenPreview() {
    MonitoringScreen(navController = rememberNavController(), viewModel = SharedMqttViewModel())
}