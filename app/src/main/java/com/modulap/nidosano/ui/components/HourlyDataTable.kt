package com.modulap.nidosano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn // Para scroll
import androidx.compose.foundation.lazy.itemsIndexed // Para LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.modulap.nidosano.data.model.HourlyRecord // Asegúrate de que esta importación sea correcta

import com.modulap.nidosano.ui.theme.BackgroundCream // Asumiendo que existe
import com.modulap.nidosano.ui.theme.BackgroundLight // Asumiendo que existe
import com.modulap.nidosano.ui.theme.TextGray // Asumiendo que existe
import com.modulap.nidosano.ui.theme.TextPrimary // Asumiendo que existe
import com.modulap.nidosano.ui.theme.TextTitleOrange // Asumiendo que existe

@Composable
fun HourlyDataTable(records: List<HourlyRecord>) {
    Column(
        modifier = Modifier
            .fillMaxWidth() // Usa fillMaxWidth, no fillMaxSize, para que se adapte al contenido en su padre.
            .padding(horizontal = 16.dp) // Márgenes laterales para toda la tabla
            .background(Color.Transparent)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundCream),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround // Distribución del encabezado
            ) {
                TableHeaderTextHour(text = "Hora", modifier = Modifier.weight(0.9f), textAlign = TextAlign.Start)
                TableHeaderText(text = "Temp.", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                TableHeaderText(text = "Humedad", modifier = Modifier.weight(1.1f), textAlign = TextAlign.Start)
                TableHeaderText(text = "Luz", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                TableHeaderText(text = "Aire", modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            itemsIndexed(records) { index, record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableDataTextHour(
                            text = record.hour,
                            modifier = Modifier.weight(0.9f),
                            color = TextTitleOrange, // Naranja para la hora
                            isHour = true,
                            textAlign = TextAlign.Start
                        )
                        TableDataTextHour(
                            text = record.temperature,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        TableDataTextHour(
                            text = record.humidity,
                            modifier = Modifier.weight(1.1f),
                            textAlign = TextAlign.Start
                        )
                        TableDataTextHour(
                            text = record.lightingLevel,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        TableDataTextHour(
                            text = record.airQuality,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TableHeaderTextHour(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = TextPrimary
        ),
        textAlign = textAlign,
        maxLines = 1
    )
}

@Composable
fun TableDataTextHour(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextGray,
    isHour: Boolean = false,
    textAlign: TextAlign
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = if (isHour) 14.sp else 14.sp,
            fontWeight = if (isHour) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        ),
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false
    )
}