package com.modulap.nidosano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape // <-- Asegúrate de importar RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // <-- Asegúrate de importar Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.White
import com.modulap.nidosano.ui.theme.OrangePrimary // <-- Probablemente necesites OrangePrimary para el foco

@Composable
fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Titulo de input
        Text(text = label, style = MaterialTheme.typography.titleMedium.copy(color = TextGray)) // Usar TextGray
        // Puedes ajustar el estilo del título si es necesario
        // style = MaterialTheme.typography.labelLarge, color = TextGray, fontWeight = FontWeight.SemiBold
        // como en tu ProfileTextField original si quieres ese look.

        // Card que envuelve el text field
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            // Ajustar la forma para que los bordes sean redondeados como en la imagen
            shape = RoundedCornerShape(12.dp), // Similar al input de la imagen
            elevation = CardDefaults.cardElevation(2.dp), // Un poco de elevación para el efecto de "borde"
            colors = CardDefaults.cardColors(containerColor = White) // Fondo de la tarjeta blanca
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White), // Fondo del TextField en blanco
                // Placeholder del text input
                placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = TextGray) },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                // Colores para TextField (no OutlinedTextField)
                colors = TextFieldDefaults.colors(
                    // Colores del fondo del TextField
                    unfocusedContainerColor = White,
                    focusedContainerColor = White,
                    disabledContainerColor = White,
                    errorContainerColor = White,

                    // Colores del "indicator" (la línea inferior)
                    // Para que no se vea una línea, puedes hacerla transparente o del mismo color del fondo
                    unfocusedIndicatorColor = Color.Transparent, // <<-- CAMBIO CLAVE: Hace la línea invisible
                    focusedIndicatorColor = OrangePrimary, // <<-- O podrías usar OrangePrimary para una línea de foco
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = MaterialTheme.colorScheme.error, // Color de error por defecto

                    // Colores del texto y cursor
                    focusedTextColor = TextGray,
                    unfocusedTextColor = TextGray,
                    disabledTextColor = TextGray,
                    errorTextColor = TextGray,
                    cursorColor = OrangePrimary,
                    errorCursorColor = OrangePrimary,

                    // Colores de la etiqueta (label)
                    focusedLabelColor = OrangePrimary, // Cuando está enfocado
                    unfocusedLabelColor = TextGray, // Cuando no está enfocado
                    disabledLabelColor = TextGray,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                ),
                singleLine = true
            )
        }
    }
}