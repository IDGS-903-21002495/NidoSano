package com.modulap.nidosano.ui.screens.tips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DeviceThermostat
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.modulap.nidosano.ui.theme.TextTitleOrange
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.modulap.nidosano.R



@Composable
fun TipDetailScreen(
    title: String,
    recomendation: String,
    measures: String,
    type: String,
    onBackClick: () -> Unit // Nuevo parámetro para manejar el regreso
) {
    val (iconPainter, cardColor) = when (type.lowercase()) {
        "salud" -> Pair(
            painterResource(id = R.drawable.salud),
            Color(0xFFE8F5E9)
        )
        "temperatura" -> Pair(
            painterResource(id = R.drawable.temperaturatp),
            Color(0xFFE3F2FD)
        )
        "amenazas frecuentes" -> Pair(
            painterResource(id = R.drawable.raton),
            Color(0xFFFFEBEE)
        )
        "limpieza" -> Pair(
            painterResource(id = R.drawable.limpieza),
            Color(0xFFF3E5F5)
        )
        "alimentacion" -> Pair(painterResource(R.drawable.alimento_tip), Color(0xFFFFF59D) )

        else -> Pair(
            painterResource(id = R.drawable.pollo_tip),
            Color(0xFFFAFAFA)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Fila para el botón de regreso y título
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            // Botón de regreso
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Regresar",
                    tint = Color.Black
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp  // ajustar según tamaño de fuente
                ),
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                maxLines = 2
            )


        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tarjeta de Recomendación
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recomendación",
                    style = MaterialTheme.typography.titleMedium.copy( // Tamaño más pequeño
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Black, // Texto en negro
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Start
                )

                // Icono más grande (80dp)
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(vertical = 8.dp),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = recomendation,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tarjeta de Medidas
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Medidas Recomendadas",
                    style = MaterialTheme.typography.titleMedium.copy( // Tamaño más pequeño
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.Black, // Texto en negro
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    textAlign = TextAlign.Start
                )

                Text(
                    text = measures,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Justify,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}