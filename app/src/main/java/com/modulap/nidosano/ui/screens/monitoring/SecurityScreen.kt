// ui/screens/monitoring/SecurityScreen.kt
package com.modulap.nidosano.ui.screens.monitoring

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.modulap.nidosano.R
import com.modulap.nidosano.data.repository.MQTTManagerHiveMQ
import com.modulap.nidosano.ui.components.BottomNavBar
import com.modulap.nidosano.ui.theme.RedDanger
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.GreenSuccess
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.navigation.Routes
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.viewmodel.SecurityViewModel

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun SecurityScreen(
    navController: NavHostController,
    viewModel: SecurityViewModel = viewModel()
) {
    var currentRoute by remember { mutableStateOf("security") }

    val mainDisplayMessage by viewModel.mainDisplayMessage.collectAsState()
    Log.d("SecurityScreen", "mainDisplayMessage: $mainDisplayMessage")
    val movementDetected by viewModel.movementDetected.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    val circleAlertText = when (movementDetected) {
        false -> "Todo en orden"
        true -> "Movimiento detectado"
        else -> connectionStatus.toString()
    }

    val alertText = when (mainDisplayMessage) {
        "Movimiento detectado en: 0" -> "No hay movimiento detectado"
        "Movimiento detectado en: Nido" -> "Movimiento detectado en el nido"
        "Movimiento detectado en: Entrada" -> "Movimiento detectado en la entrada"
        "Movimiento detectado en: Bebedero" -> "Movimiento detectado en la zona de alimentación"
        else -> mainDisplayMessage
    }

    // Colores dinámicos para el círculo de alerta
    val circleColor by animateColorAsState(
        targetValue = when {
            connectionStatus == MQTTManagerHiveMQ.ConnectionState.ERROR -> RedDanger
            movementDetected -> RedDanger
            connectionStatus == MQTTManagerHiveMQ.ConnectionState.CONNECTED -> GreenSuccess
            else -> TextGray
        },
        animationSpec = tween(durationMillis = 500), label = "circleColorAnimation"
    )

    // Animación de pulso para el círculo central
    val pulseScale by rememberInfiniteTransition(label = "pulseAnimation").animateFloat(
        initialValue = 1f,
        targetValue = if (movementDetected) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScaleAnimation"
    )

    // Animación de alfa para las ondas cuando hay alerta
    val waveAlpha by rememberInfiniteTransition(label = "waveAlphaAnimation").animateFloat(
        initialValue = 0.15f,
        targetValue = if (movementDetected) 0.5f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "waveAlpha"
    )

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
                .padding(paddingValues),
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
                    text = "Monitoreo de seguridad",
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

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(200.dp * pulseScale)
                    .clip(CircleShape)
                    .background(circleColor)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = circleAlertText,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Ondas de alerta (sin cambios)
                if (movementDetected && connectionStatus == MQTTManagerHiveMQ.ConnectionState.CONNECTED) {
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .clip(CircleShape)
                            .background(RedDanger.copy(alpha = waveAlpha))
                            .align(Alignment.Center)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(CircleShape)
                                .background(RedDanger.copy(alpha = waveAlpha / 2))
                                .align(Alignment.Center)
                        ) {}
                    }
                }
            }

            // 💡 El botón se ha movido fuera del Box para evitar la superposición
            Spacer(modifier = Modifier.height(32.dp))

            ButtonPrimary(
                onClick = { navController.navigate(Routes.SetMonitoringSchedule) },
                text = "Configurar horario de monitoreo",
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Texto inferior:
            Text(
                text = alertText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
