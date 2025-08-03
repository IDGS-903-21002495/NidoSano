package com.modulap.nidosano.ui.screens

import android.app.TimePickerDialog
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator // Importar para el indicador de carga
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.modulap.nidosano.R
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.viewmodel.FeedingScheduleViewModel
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Calendar

@Composable
fun ScheduleFeedingScreen(
    navController: NavHostController,
    scheduleId: String? = null,
    initialHour: Int? = null,
    initialMinute: Int? = null,
    initialQuantityGrams: Int? = null,
    initialFrequency: String? = null,
    feedingScheduleViewModel: FeedingScheduleViewModel = viewModel(),
    onBackClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val decodedInitialFrequency = initialFrequency?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    }

    var selectedHour by remember { mutableStateOf(initialHour ?: calendar[Calendar.HOUR_OF_DAY]) }
    var selectedMinute by remember { mutableStateOf(initialMinute ?: calendar[Calendar.MINUTE]) }
    // Convertir la cantidad total de gramos a "porciones" para la UI (dividir por 500)
    var portions by remember { mutableStateOf(if (initialQuantityGrams != null) (initialQuantityGrams / 500).toString() else "") }
    val frequencyOptions = listOf("Diariamente", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var selectedFrequency by remember { mutableStateOf(decodedInitialFrequency ?: frequencyOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    // Observar el estado de la operación de guardado del ViewModel
    val saveScheduleStatus by feedingScheduleViewModel.saveScheduleStatus.collectAsState()

    // Manejar el estado del guardado
    LaunchedEffect(saveScheduleStatus) {
        when (saveScheduleStatus) {
            CommandStatus.Loading -> {
                // El Toast se muestra en el Composable de carga, no aquí
            }
            is CommandStatus.Error -> {
                val errorMessage = (saveScheduleStatus as CommandStatus.Error).message
                Toast.makeText(context, "Error al guardar: $errorMessage", Toast.LENGTH_LONG).show()
                feedingScheduleViewModel.resetSaveScheduleStatus()
            }
            CommandStatus.Success -> {
                Toast.makeText(context, "Programación guardada con éxito.", Toast.LENGTH_SHORT).show()
                feedingScheduleViewModel.resetSaveScheduleStatus()
                navController.popBackStack()
            }
            CommandStatus.Idle -> { /* No hacer nada */ }
        }
    }

    // TimePickerDialog ahora se crea con el contexto que ya maneja los colores del sistema
    val timePickerDialog = TimePickerDialog(
        context,
        // Al seleccionar la hora, actualiza los estados
        { _: TimePicker, hour: Int, minute: Int ->
            selectedHour = hour
            selectedMinute = minute
        },
        selectedHour, selectedMinute, false // Formato 24h: true, Formato 12h: false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Asegura un fondo blanco para toda la pantalla
    ) {
        // Top Bar
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
                text = if (scheduleId == null) "Programar alimentación" else "Editar programación",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextGray
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Horario
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Horario",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // OutlinedTextField para la HORA
                            OutlinedTextField(
                                value = String.format("%02d", if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour),
                                onValueChange = { newValue ->
                                    val hour = newValue.filter { it.isDigit() }.toIntOrNull()
                                    if (hour != null && hour >= 0 && hour <= 23) {
                                        selectedHour = hour
                                    } else if (newValue.isEmpty()) {
                                        selectedHour = 0
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(60.dp)
                                    .clickable { timePickerDialog.show() },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = OrangePrimary
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = TextGray,
                                    disabledBorderColor = TextGray,
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Text(
                                text = ":",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextGray
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            // OutlinedTextField para los MINUTOS
                            OutlinedTextField(
                                value = String.format("%02d", selectedMinute),
                                onValueChange = { newValue ->
                                    val minute = newValue.filter { it.isDigit() }.toIntOrNull()
                                    if (minute != null && minute >= 0 && minute <= 59) {
                                        selectedMinute = minute
                                    } else if (newValue.isEmpty()) {
                                        selectedMinute = 0
                                    }
                                },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(60.dp)
                                    .clickable { timePickerDialog.show() },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = OrangePrimary
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = TextGray,
                                    disabledBorderColor = TextGray,
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedHour >= 12) "PM" else "AM",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextGray
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { timePickerDialog.cancel() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text(text = "CANCEL", color = TextGray, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { timePickerDialog.show() }, // Muestra el TimePicker
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Text(text = "OK", color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Porciones",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = portions,
                    onValueChange = { newValue ->
                        portions = newValue.filter { it.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("Ingrese número de porciones") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = TextGray,
                        cursorColor = OrangePrimary,
                        focusedLabelColor = OrangePrimary,
                        unfocusedLabelColor = TextGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Frecuencia
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Frecuencia",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier.fillMaxWidth() // Quitar padding horizontal aquí
                ) {
                    OutlinedTextField(
                        value = selectedFrequency,
                        onValueChange = { /* No editable directamente */ },
                        readOnly = true,
                        label = { Text("Seleccione la frecuencia") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Expandir/Colapsar",
                                    tint = TextGray
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = TextGray,
                            focusedLabelColor = OrangePrimary,
                            unfocusedLabelColor = TextGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Color.White)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        frequencyOptions.forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency) },
                                onClick = {
                                    selectedFrequency = frequency
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth(), // Quitar padding horizontal aquí
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ButtonPrimary(
                        text = if (scheduleId == null) "Guardar" else "Actualizar",
                        onClick = {
                            val portionsValue = portions.toIntOrNull()
                            if (portionsValue != null && portionsValue > 0) {
                                val finalQuantityGrams = portionsValue * 500
                                feedingScheduleViewModel.saveFeedingSchedule(
                                    id = scheduleId,
                                    hour = selectedHour,
                                    minute = selectedMinute,
                                    duration = finalQuantityGrams,
                                    frequency = selectedFrequency
                                )
                            } else {
                                Toast.makeText(context, "Por favor, ingrese un número de porciones válido y mayor a 0.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = saveScheduleStatus != CommandStatus.Loading
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Indicador de carga superpuesto
            if (saveScheduleStatus == CommandStatus.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)), // Fondo semi-transparente
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }
    }
}
