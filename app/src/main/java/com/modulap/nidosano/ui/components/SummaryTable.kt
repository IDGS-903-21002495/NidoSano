package com.modulap.nidosano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.modulap.nidosano.data.model.DailySummary
import com.modulap.nidosano.ui.theme.BackgroundCream
import com.modulap.nidosano.ui.theme.BackgroundLight
import com.modulap.nidosano.ui.theme.OrangeSecondary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextPrimary
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.White

@Composable
fun SummaryTable(summaries: List<DailySummary>, onViewMoreClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.Transparent)
    ) {
        // Encabezado de la tabla
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
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                TableHeaderText(text = "Fecha", modifier = Modifier.weight(1.5f))
                TableHeaderText(text = "Temperatura", modifier = Modifier.weight(1f))
                TableHeaderText(text = "Humedad", modifier = Modifier.weight(1f))
                TableHeaderText(text = "Acciones", modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filas de datos
        summaries.forEachIndexed { index, summary ->
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

                    TableDataText(
                        text = summary.date,
                        modifier = Modifier.weight(1.5f),
                        color = TextTitleOrange,
                        isDate = true,
                        textAlign = TextAlign.Start
                    )
                    TableDataText(
                        text = summary.temperature,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    TableDataText(
                        text = summary.humidity,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )

                    Button(
                        onClick = { onViewMoreClick(summary.date) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeSecondary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            "Más",
                            color = White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            if (index < summaries.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun TableHeaderText(text: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
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
fun TableDataText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = TextGray,
    isDate: Boolean = false,
    textAlign: TextAlign
) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodyMedium.copy(
            fontSize = if (isDate) 14.sp else 14.sp,
            fontWeight = if (isDate) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        ),
        maxLines = 1,
        softWrap = false
    )
}