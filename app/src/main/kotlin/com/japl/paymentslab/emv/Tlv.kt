package com.japl.paymentslab.emv

data class Tlv(val tag: String, val value: ByteArray) {
    // ByteArray in a data class compares by reference: override so == works
    override fun equals(other: Any?) =
        other is Tlv && tag == other.tag && value.contentEquals(other.value)

    override fun hashCode() = 31 * tag.hashCode() + value.contentHashCode()
}

/**
 * Parses TLV (Tag, Length, Value) from EMV field 55.
 * Throws IllegalArgumentException if the data is truncated or the length is unsupported.
 */
fun parseTlv(data: ByteArray): List<Tlv> {
    val result = mutableListOf<Tlv>()
    var pos = 0

    while (pos < data.size) {
        // ---- TAG ----
        val tagStart = pos
        val first = data[pos++].toInt() and 0xFF
        if ((first and 0x1F) == 0x1F) {
            // Multibyte tag: keep reading while the high bit of the byte just read is set
            var more: Boolean
            do {
                require(pos < data.size) { "Truncated tag at offset $tagStart" }
                more = (data[pos++].toInt() and 0x80) != 0
            } while (more)
        }
        val tag = data.copyOfRange(tagStart, pos)
            .joinToString("") { "%02X".format(it.toInt() and 0xFF) }

        // ---- LENGTH ----
        require(pos < data.size) { "Truncated length for tag $tag" }
        val l0 = data[pos++].toInt() and 0xFF
        val length = when {
            l0 < 0x80 -> l0
            l0 == 0x81 -> {
                require(data.size - pos >= 1) { "Truncated length (0x81) for tag $tag" }
                data[pos++].toInt() and 0xFF
            }
            l0 == 0x82 -> {
                require(data.size - pos >= 2) { "Truncated length (0x82) for tag $tag" }
                val hi = data[pos++].toInt() and 0xFF
                val lo = data[pos++].toInt() and 0xFF
                (hi shl 8) or lo
            }
            else -> throw IllegalArgumentException(
                "Unsupported length byte 0x%02X for tag %s".format(l0, tag),
            )
        }

        // ---- VALUE ----
        // Compare with a subtraction (not pos + length) so it can never overflow.
        require(length <= data.size - pos) {
            "Truncated value for tag $tag: need $length bytes, have ${data.size - pos}"
        }
        result += Tlv(tag, data.copyOfRange(pos, pos + length))
        pos += length
    }
    return result
}

/** A tag is constructed if bit 0x20 of its first byte is set: its value contains more TLVs. */
fun Tlv.isConstructed(): Boolean =
    (tag.substring(0, 2).toInt(16) and 0x20) != 0

fun Tlv.children(): List<Tlv> =
    if (isConstructed()) parseTlv(value) else emptyList()
