package com.modulap.nidosano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.modulap.nidosano.ui.theme.TextGray
import com.modulap.nidosano.ui.theme.White

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
        Text(text = label, style = MaterialTheme.typography.titleMedium)

        // Card que envuelve el text field
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White),
                // Placeholder del text input
                placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium, color = TextGray) },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = White,
                    focusedContainerColor = White,
                    disabledContainerColor = White,
                    errorContainerColor = White,

                    unfocusedIndicatorColor = White,
                    focusedIndicatorColor = White,
                    disabledIndicatorColor = White,
                    errorIndicatorColor = White
                ),
                singleLine = true
            )
        }
    }
}
