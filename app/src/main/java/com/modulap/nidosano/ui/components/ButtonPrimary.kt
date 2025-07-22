package com.modulap.nidosano.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.modulap.nidosano.ui.theme.OrangePrimary

@Composable
fun ButtonPrimary(
    text: String,
    onClick: () -> Unit,
    cornerRadius: Dp = 8.dp,
    paddingHorizontal: Dp = 16.dp,
    paddingVertical: Dp = 14.dp
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        contentPadding = PaddingValues(
            horizontal = paddingHorizontal,
            vertical = paddingVertical
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = OrangePrimary,
            contentColor = Color.White
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium)
    }
}
