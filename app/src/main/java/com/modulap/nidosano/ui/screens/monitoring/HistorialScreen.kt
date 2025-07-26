package com.modulap.nidosano.ui.screens.monitoring

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.modulap.nidosano.ui.components.SummaryTable
import com.modulap.nidosano.viewmodel.HistoryViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray

@Composable
fun HistorialScreen(
    navController: NavHostController,
    userId: String,
    coopId: String,
    onViewMoreClick: (String) -> Unit,
    viewModel: HistoryViewModel = viewModel()
) {
    val state = viewModel.state

    // Disparar carga solo una vez
    LaunchedEffect(Unit) {
        viewModel.loadSummaries(userId, coopId)
        println("En la tabla llegaron estos summaries: ${state.summaries}")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Barra Superior Personalizada (Top Bar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White) // Mantener este fondo blanco para la consistencia
                .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                // Lógica para navegar hacia atrás
                navController.popBackStack()
                Log.d("HistorialScreen", "Navegando hacia atrás.")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.angulo_izquierdo),
                    contentDescription = "Regresar",
                    tint = TextGray,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Historial",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp, // **Ya tiene el fontSize correcto**
                    color = TextGray // **Ya tiene el color correcto**
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido principal (Tabla o mensajes de estado)
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp), // Márgenes laterales para el contenido
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando historial...",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextGray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp
                    )
                )
            }
        } else if (state.summaries.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp), // Márgenes laterales para el contenido
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No hay datos de historial disponibles.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = OrangePrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Vuelve más tarde o verifica tu conexión.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextGray
                    )
                )
            }
        } else {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ){
                SummaryTable(state.summaries, onViewMoreClick)
            }

        }
    }
}