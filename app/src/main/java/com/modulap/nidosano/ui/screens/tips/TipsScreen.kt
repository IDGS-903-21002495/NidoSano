package com.modulap.nidosano.ui.screens.tips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.components.BottomNavBar
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.modulap.nidosano.data.model.Tip
import com.modulap.nidosano.viewmodel.TipsViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource

// Modelo visual usando ImageVector y color para icono
data class TipVisual(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color // color para el icono

)

@Composable
fun TipsScreen(
    navController: NavHostController,
    viewModel: TipsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var currentRoute by remember { mutableStateOf("tips") }
    val state = viewModel.state

    LaunchedEffect(Unit) {
        viewModel.loadTip()
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
                    text = "Consejos",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextTitleOrange,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

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
                text = "Cuida mejor a tus gallinas",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    color = TextGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                textAlign = TextAlign.Center
            )

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                state.tip.forEach { tip ->
                    TipCardFromFirebase(tip = tip) {
                        navController.navigate(
                            "tipDetail/${tip.title}/${tip.recomendation}/${tip.measures}/${tip.type}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp)) // Añade espacio al final
            }

        }
    }
}

@Composable
fun TipCardFromFirebase(tip: Tip, onClick: () -> Unit) {
    // Selecciona el recurso drawable y colores según el tipo
    val (imageResId, primaryColor, backgroundColor) = when (tip.type.lowercase()) {
        "salud" -> Triple(R.drawable.salud, Color(0xFF388E3C), Color(0xFFE8F5E9))       // Verde
        "temperatura" -> Triple(R.drawable.temperaturatp, Color(0xFF1976D2), Color(0xFFE3F2FD)) // Azul
        "amenazas frecuentes" -> Triple(R.drawable.raton, Color(0xFFD32F2F), Color(0xFFFFEBEE))         // Rojo
        "limpieza" -> Triple(R.drawable.limpieza, Color(0xFF7B1FA2), Color(0xFFF3E5F5))    // Morado
        "alimentacion" -> Triple(R.drawable.alimento_tip, Color(0xFFF9A825), Color(0xFFFFF9C4)) // Amarillo

        else -> Triple(R.drawable.pollo_tip, Color.DarkGray, Color(0xFFF5F5F5))             // Gris
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Imagen con fondo circular
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(primaryColor.copy(alpha = 0.15f), shape = RoundedCornerShape(50))
                    .wrapContentSize(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = tip.type,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tip.recomendation,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = Color.DarkGray,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Ver más",
                tint = primaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
