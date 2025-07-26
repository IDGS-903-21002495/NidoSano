package com.modulap.nidosano.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Necesario para NavHostController en Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController // Necesario para NavHostController en Preview
import com.modulap.nidosano.R

import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.BackgroundLight

// Modelo de datos para una programación de alimentación (se mantiene igual)
data class FeedingSchedule(
    val id: String,
    val hour: Int,
    val minute: Int,
    val quantityGrams: Int,
    val frequency: String
)

@Composable
fun ScheduleListScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    onEditScheduleClick: (FeedingSchedule) -> Unit = {},
    onDeleteScheduleClick: (FeedingSchedule) -> Unit = {}
) {
    var schedules by remember { mutableStateOf(listOf(
        FeedingSchedule("1", 7, 0, 100, "Diariamente"),
        FeedingSchedule("2", 12, 30, 150, "Lunes, Miércoles, Viernes"),
        FeedingSchedule("3", 18, 0, 120, "Diariamente")
    )) }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.angulo_izquierdo),
                        contentDescription = "Volver atrás",
                        tint = TextGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mis programaciones",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextGray
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (schedules.isEmpty()) {
                    item {
                        Text(
                            text = "No hay programaciones activas. ¡Pulsa '+' para añadir una!",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = TextGray
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(schedules, key = { it.id }) { schedule ->
                        ScheduleCard(
                            schedule = schedule,
                            onEditClick = onEditScheduleClick,
                            onDeleteClick = {
                                schedules = schedules.filter { s -> s.id != it.id }
                                onDeleteScheduleClick(it)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button - Ajustado el padding inferior
        FloatingActionButton(
            onClick = {navController.navigate("feeding_schedule")},
            modifier = Modifier
                .padding(bottom = 80.dp, end = 24.dp) // Aumentado significativamente el padding inferior
                .align(Alignment.BottomEnd),
            containerColor = OrangePrimary,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, "Añadir programación", tint = Color.White)
        }
    }
}

// ScheduleCard se mantiene sin cambios
@Composable
fun ScheduleCard(
    schedule: FeedingSchedule,
    onEditClick: (FeedingSchedule) -> Unit,
    onDeleteClick: (FeedingSchedule) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d:%02d", schedule.hour, schedule.minute),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = OrangePrimary
                    )
                )
                Row {
                    IconButton(onClick = { onEditClick(schedule) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint = TextGray
                        )
                    }
                    IconButton(onClick = { onDeleteClick(schedule) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = TextGray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Cantidad: ${schedule.quantityGrams} gramos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TextGray
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Frecuencia: ${schedule.frequency}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    color = TextGray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScheduleListScreen() {
    ScheduleListScreen(navController = rememberNavController())
}