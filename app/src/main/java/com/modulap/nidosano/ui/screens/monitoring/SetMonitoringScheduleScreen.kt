package com.modulap.nidosano.ui.screens.monitoring

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi // Make sure this import is present
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape // Import for custom shape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.modulap.nidosano.data.model.CommandStatus
import com.modulap.nidosano.ui.components.ButtonPrimary
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.viewmodel.SecurityViewModel
import com.modulap.nidosano.ui.theme.TextTitleOrange
import com.modulap.nidosano.ui.theme.OrangePrimary // Import OrangePrimary

@RequiresApi(Build.VERSION_CODES.N) // <--- ADD THIS LINE BACK
@Composable
fun SetMonitoringScheduleScreen(
    navController: NavHostController,
    onBackClick: () -> Unit = {},
    viewModel: SecurityViewModel
) {
    var startHourInput by remember { mutableStateOf("") }
    var endHourInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observar el estado de la operación de guardar desde el ViewModel
    val saveScheduleStatus by viewModel.saveScheduleStatus.collectAsState()

    // Manejar el estado del guardar
    LaunchedEffect(saveScheduleStatus) {
        when (saveScheduleStatus) {
            is CommandStatus.Success -> {
                onBackClick()
            }
            is CommandStatus.Error -> {
                errorMessage = (saveScheduleStatus as CommandStatus.Error).message
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
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
                text = "Horario de monitoreo",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextGray
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card style changes
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Horario de monitoreo (0-23)",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = TextGray
                    ),
                    modifier = Modifier
                        .fillMaxWidth() // Fill width for consistent alignment with title in other screen
                        .padding(bottom = 12.dp) // Adjusted padding
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Campo de entrada para la hora de inicio con la elevación y el color de fondo
                    OutlinedTextField(
                        value = startHourInput,
                        onValueChange = { newValue ->
                            startHourInput = newValue.filter { it.isDigit() }
                        },
                        label = { Text("Hora de inicio") },
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp), // Adjusted height for consistency
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = OrangePrimary // Consistent text color for input
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = OrangePrimary, // Consistent border color
                            unfocusedBorderColor = TextGray, // Consistent border color
                            cursorColor = OrangePrimary, // Consistent cursor color
                            focusedLabelColor = OrangePrimary, // Consistent label color
                            unfocusedLabelColor = TextGray // Consistent label color
                        ),
                        shape = RoundedCornerShape(8.dp) // Explicitly 8.dp rounded corners
                    )

                    // Box para el separador
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(70.dp), // Adjusted height for consistency
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineLarge.copy( // Updated text style
                                fontWeight = FontWeight.Bold, // Added Bold for consistency
                                color = TextGray // Consistent text color
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Campo de entrada para la hora de fin con la elevación y el color de fondo
                    OutlinedTextField(
                        value = endHourInput,
                        onValueChange = { newValue ->
                            endHourInput = newValue.filter { it.isDigit() }
                        },
                        label = { Text("Hora de fin") },
                        modifier = Modifier
                            .weight(1f)
                            .height(70.dp), // Adjusted height for consistency
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.headlineMedium.copy( // Applied text style for value
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = OrangePrimary // Consistent text color for input
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = OrangePrimary, // Consistent border color
                            unfocusedBorderColor = TextGray, // Consistent border color
                            cursorColor = OrangePrimary, // Consistent cursor color
                            focusedLabelColor = OrangePrimary, // Consistent label color
                            unfocusedLabelColor = TextGray // Consistent label color
                        ),
                        shape = RoundedCornerShape(8.dp) // Explicitly 8.dp rounded corners
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                ButtonPrimary(
                    onClick = {
                        val startHour = startHourInput.toIntOrNull()
                        val endHour = endHourInput.toIntOrNull()

                        if (startHour != null && endHour != null && startHour in 0..23 && endHour in 0..23) {
                            viewModel.saveMonitoringSchedule(startHour, endHour)
                        } else {
                            errorMessage = "Por favor, ingrese horas válidas (0-23)."
                        }
                    },
                    text = "Guardar",
                    enabled = saveScheduleStatus != CommandStatus.Loading
                )
            }
        }
    }
}

