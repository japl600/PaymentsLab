package com.japl.paymentslab.framing

/** Builds valid frames (or ones with a deliberately corrupted LRC) to test the parser. */
object FrameBuilder {

    fun build(data: ByteArray, badLrc: Boolean = false): ByteArray {
        require(data.size <= 0xFFFF) { "DATA demasiado largo para un LEN de 2 bytes" }
        val len = data.size
        val body = byteArrayOf((len shr 8).toByte(), len.toByte()) + data + 0x03.toByte()

        var lrc = 0
        for (b in body) lrc = lrc xor (b.toInt() and 0xFF)
        if (badLrc) lrc = lrc xor 0xFF

        return byteArrayOf(0x02) + body + lrc.toByte()
    }
}
