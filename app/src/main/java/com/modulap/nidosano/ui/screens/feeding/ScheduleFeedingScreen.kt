package com.modulap.nidosano.ui.screens

import android.app.TimePickerDialog
import android.widget.TimePicker
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.navigation.NavHostController
import com.modulap.nidosano.R
import com.modulap.nidosano.ui.components.ButtonPrimary

import java.util.Calendar
import com.modulap.nidosano.ui.theme.OrangePrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.White

@Composable
fun ScheduleFeedingScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedHour by remember { mutableStateOf(calendar[Calendar.HOUR_OF_DAY]) }
    var selectedMinute by remember { mutableStateOf(calendar[Calendar.MINUTE]) }

    val timePickerDialog = TimePickerDialog(
        context,
        { _: TimePicker, hour: Int, minute: Int ->
            selectedHour = hour
            selectedMinute = minute
        }, selectedHour, selectedMinute, false
    )

    var foodQuantity by remember { mutableStateOf("") }
    val frequencyOptions = listOf("Diariamente", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var selectedFrequency by remember { mutableStateOf(frequencyOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = "Programar alimentación",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextGray
                ),
                modifier = Modifier.weight(1f)
            )
        }

        // Contenido principal de la pantalla que debe ser desplazable
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ocupa todo el espacio disponible, empujando el botón "Guardar" hacia abajo
                .verticalScroll(rememberScrollState()), // Permite el desplazamiento del contenido
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
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
                        OutlinedTextField(
                            value = String.format("%02d", if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour),
                            onValueChange = { /* No editable directamente */ },
                            modifier = Modifier
                                .width(80.dp)
                                .height(60.dp)
                                .clickable { timePickerDialog.show() },
                            readOnly = true,
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
                        OutlinedTextField(
                            value = String.format("%02d", selectedMinute),
                            onValueChange = { /* No editable directamente */ },
                            modifier = Modifier
                                .width(80.dp)
                                .height(60.dp)
                                .clickable { timePickerDialog.show() },
                            readOnly = true,
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
                            onClick = { timePickerDialog.show() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text(text = "OK", color = OrangePrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Cantidad de alimento
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Cantidad de alimento (gramos)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = foodQuantity,
                onValueChange = { newValue ->
                    foodQuantity = newValue.filter { it.isDigit() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("Ingrese cantidad") },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
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
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ButtonPrimary(
                    text = "Guardar",
                    onClick = { /* Acción al hacer clic en "Guardar" */ },
                )
            }

        }


    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScheduleFeedingScreen() {
    ScheduleFeedingScreen(navController = NavHostController(LocalContext.current))
}