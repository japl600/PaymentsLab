package com.japl.paymentslab.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.japl.paymentslab.pan.isValidLuhn
import com.japl.paymentslab.pan.maskPan

private val PAN_SAMPLES = listOf(
    "Visa de prueba" to "4111111111111111",
    "Mastercard" to "5500005555555559",
    "Amex (15)" to "378282246310005",
    "Luhn inválido" to "4111111111111112",
    "Corto" to "4111",
    "No numérico" to "4111abcd11111111",
    "Vacío" to "",
)

@Composable
fun PanScreen() {
    var pan by rememberSaveable { mutableStateOf("4111111111111111") }

    val masked = runCatching { maskPan(pan) }

    ScreenColumn {
        Text("maskPan e isValidLuhn", style = MaterialTheme.typography.titleMedium)
        Text("Usa solo números de tarjeta de prueba, nunca uno real.", style = MaterialTheme.typography.bodySmall)

        OutlinedTextField(
            value = pan,
            onValueChange = { pan = it },
            label = { Text("PAN") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        ResultRow(
            label = "maskPan(pan)",
            value = masked.getOrElse { "IllegalArgumentException: ${it.message}" },
            isError = masked.isFailure,
        )
        ResultRow("isValidLuhn(pan)", isValidLuhn(pan).toString())

        SampleChips(PAN_SAMPLES) { pan = it }
    }
}
