package com.modulap.nidosano.ui.screens.monitoring

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.modulap.nidosano.viewmodel.HourlyViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.modulap.nidosano.R

// Importa tus colores personalizados
import com.modulap.nidosano.ui.components.HourlyDataTable
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextGrayLight // Ya importado, excelente
// import com.modulap.nidosano.ui.theme.TextTitleOrange // Ya no se usa para títulos de detalle

@Composable
fun HistorialDetailScreen(
    userId: String,
    coopId: String,
    date: String,
    navController: NavHostController,
    viewModel: HourlyViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        viewModel.loadHourlyData(userId, coopId, date)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Barra Superior Personalizada (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Asegura fondo blanco
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.angulo_izquierdo),
                    contentDescription = "Atrás",
                    tint = TextGray,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Resumen por hora", // Título más corto, la fecha se muestra debajo
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp, // **Tamaño de fuente consistente**
                    color = TextGray // **Color de texto consistente**
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Texto de la fecha debajo de la barra superior
        Text(
            text = date,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = TextGray
            ),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 16.dp)
        )

        if (viewModel.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando datos horarios...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextGray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                )
            }
        } else if (viewModel.state.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No hay datos horarios disponibles para esta fecha.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Intenta con otra fecha o verifica la información.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextGray
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            Column (
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ){
                HourlyDataTable(records = viewModel.state)
            }

        }
    }
}