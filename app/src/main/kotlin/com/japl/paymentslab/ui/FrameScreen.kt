package com.japl.paymentslab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.japl.paymentslab.framing.FrameBuilder
import com.japl.paymentslab.framing.FrameParser
import com.japl.paymentslab.util.hexToBytes
import com.japl.paymentslab.util.toHex

private const val MAX_LOG_ENTRIES = 100

/** Parser + log. Lives outside the screen so it is not lost on tab switches. */
class FrameLabState {
    private var parser = FrameParser()

    var log by mutableStateOf(emptyList<String>())
        private set

    fun feed(bytes: ByteArray) {
        record("feed(${bytes.size} bytes)", parser.feed(bytes))
    }

    fun feedByteByByte(bytes: ByteArray) {
        record("feed byte a byte (${bytes.size} bytes)", bytes.flatMap { parser.feed(byteArrayOf(it)) })
    }

    private fun record(what: String, frames: List<ByteArray>) {
        val details = frames.joinToString(separator = "") {
            "\n   DATA: ${it.toHex().ifEmpty { "(vacío)" }}   \"${it.toPrintable()}\""
        }
        log = (listOf("$what -> ${frames.size} trama(s)$details") + log).take(MAX_LOG_ENTRIES)
    }

    fun reset() {
        parser = FrameParser()
        log = listOf("Parser reiniciado (buffer vacío)")
    }
}

private fun ByteArray.toPrintable(): String =
    map { b -> val c = (b.toInt() and 0xFF).toChar(); if (c in ' '..'~') c else '·' }.joinToString("")

@Composable
fun FrameScreen(state: FrameLabState) {
    var hex by rememberSaveable { mutableStateOf(FrameBuilder.build("Hola".toByteArray()).toHex()) }
    var text by rememberSaveable { mutableStateOf("Hola") }
    var badLrc by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun withBytes(action: (ByteArray) -> Unit) {
        try {
            action(hexToBytes(hex))
            error = null
        } catch (e: IllegalArgumentException) {
            error = e.message
        }
    }

    ScreenColumn {
        Text("FrameParser.feed", style = MaterialTheme.typography.titleMedium)
        Text(
            "STX | LEN (2 bytes) | DATA | ETX | LRC. El parser conserva su buffer entre pulsaciones: " +
                "alimenta la trama en dos mitades para probar la fragmentación.",
            style = MaterialTheme.typography.bodySmall,
        )

        OutlinedTextField(
            value = hex,
            onValueChange = { hex = it },
            label = { Text("Bytes a alimentar (hex)") },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { withBytes(state::feed) }) { Text("Alimentar") }
            OutlinedButton(onClick = { withBytes(state::feedByteByByte) }) { Text("Byte a byte") }
            OutlinedButton(onClick = state::reset) { Text("Reiniciar") }
        }

        HorizontalDivider()

        Text("Generador de tramas", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Texto del DATA") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = badLrc, onCheckedChange = { badLrc = it })
            Text("LRC incorrecto")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                hex = FrameBuilder.build(text.toByteArray(), badLrc).toHex()
                error = null
            }) { Text("Generar") }
            OutlinedButton(onClick = {
                val frame = FrameBuilder.build(text.toByteArray(), badLrc).toHex()
                hex = "$frame 55 10 $frame"          // two frames with garbage in between
                error = null
            }) { Text("Dos tramas + basura") }
        }

        HorizontalDivider()

        Text("Bitácora (lo más reciente arriba)", style = MaterialTheme.typography.titleSmall)
        if (state.log.isEmpty()) {
            Text("Todavía no has alimentado nada.", style = MaterialTheme.typography.bodySmall)
        }
        state.log.forEach { ResultRow(label = "", value = it) }
    }
}
