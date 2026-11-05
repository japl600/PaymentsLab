package com.japl.paymentslab.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.japl.paymentslab.emv.Tlv
import com.japl.paymentslab.emv.children
import com.japl.paymentslab.emv.isConstructed
import com.japl.paymentslab.emv.parseTlv
import com.japl.paymentslab.util.hexToBytes
import com.japl.paymentslab.util.toHex

private const val MAX_DEPTH = 8

private val TLV_SAMPLES = listOf(
    "Vector del enunciado" to "9F2608 1122334455667788 9F2701 80 9505 0000008000",
    "Constructed (77)" to "77 04 9F2701 80",
    "Longitud 0x81" to "9F26 81 08 1122334455667788",
    "Tag de 3 bytes" to "DF8101 02 AABB",
    "Valor truncado" to "95 05 0000",
    "Tag truncado" to "9F",
    "Longitud 0x80" to "95 80",
)

private val EMV_TAG_NAMES = mapOf(
    "5A" to "Application PAN",
    "57" to "Track 2 Equivalent Data",
    "77" to "Response Message Template Format 2",
    "82" to "Application Interchange Profile",
    "95" to "Terminal Verification Results",
    "9A" to "Transaction Date",
    "9C" to "Transaction Type",
    "5F2A" to "Transaction Currency Code",
    "9F02" to "Amount, Authorised",
    "9F10" to "Issuer Application Data",
    "9F1A" to "Terminal Country Code",
    "9F26" to "Application Cryptogram",
    "9F27" to "Cryptogram Information Data",
    "9F33" to "Terminal Capabilities",
    "9F36" to "Application Transaction Counter",
    "9F37" to "Unpredictable Number",
)

@Composable
fun TlvScreen() {
    var hex by rememberSaveable { mutableStateOf(TLV_SAMPLES.first().second) }

    // First hex -> bytes (may fail), then parseTlv (may fail): both errors are shown the same way.
    val result = runCatching { parseTlv(hexToBytes(hex)) }

    ScreenColumn {
        Text("parseTlv", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = hex,
            onValueChange = { hex = it },
            label = { Text("Datos en hexadecimal") },
            modifier = Modifier.fillMaxWidth(),
        )

        result.fold(
            onSuccess = { list ->
                Text("${list.size} TLV", style = MaterialTheme.typography.titleSmall)
                list.forEach { TlvNode(it, depth = 0) }
            },
            onFailure = {
                ResultRow(
                    label = "Resultado",
                    value = "${it::class.simpleName}: ${it.message}",
                    isError = true,
                )
            },
        )

        SampleChips(TLV_SAMPLES) { hex = it }
    }
}

@Composable
private fun TlvNode(tlv: Tlv, depth: Int) {
    val name = EMV_TAG_NAMES[tlv.tag]
    Column(Modifier.padding(start = (depth * 16).dp)) {
        Text(
            text = if (name != null) "${tlv.tag}  ·  $name" else tlv.tag,
            style = MaterialTheme.typography.titleSmall,
        )
        Text(
            text = "len = ${tlv.value.size}   value = ${tlv.value.toHex().ifEmpty { "(vacío)" }}",
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (tlv.isConstructed() && depth < MAX_DEPTH) {
            runCatching { tlv.children() }.fold(
                onSuccess = { children -> children.forEach { TlvNode(it, depth + 1) } },
                onFailure = {
                    Text(
                        text = "Hijos inválidos: ${it.message}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
            )
        }
    }
}
