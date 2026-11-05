package com.japl.paymentslab.util

/** "9F26 08 ff" -> bytes. Ignores spaces; throws IllegalArgumentException if it is not valid hex. */
fun hexToBytes(text: String): ByteArray {
    val clean = text.filterNot { it.isWhitespace() }
    require(clean.length % 2 == 0) { "El hexadecimal debe tener una cantidad par de dígitos" }
    require(clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
        "Hay caracteres que no son hexadecimales"
    }
    return ByteArray(clean.length / 2) { clean.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
}

/** bytes -> "9F 26 08" (uppercase, space-separated). */
fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
