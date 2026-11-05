package com.japl.paymentslab.framing

/**
 * Serial frame parser: STX | LEN (2 bytes, big-endian) | DATA (LEN bytes) | ETX | LRC
 * LRC = XOR of all bytes from LEN through ETX, inclusive.
 *
 * Not thread-safe: its buffer is mutable, so `feed` must be called from a single thread.
 */
class FrameParser {

    private companion object {
        const val STX: Byte = 0x02
        const val ETX: Byte = 0x03
        const val HEADER_SIZE = 3    // STX + LEN (2 bytes)
        const val TRAILER_SIZE = 2   // ETX + LRC
        const val MAX_LEN = 1024     // larger LEN => invalid frame
    }

    // Accumulates bytes across feed() calls. `pos` (local) is just the index into it.
    private var buffer = ByteArray(0)

    /** Returns the DATA of every complete, valid frame that can be extracted from the buffer. */
    fun feed(bytes: ByteArray): List<ByteArray> {
        buffer += bytes
        val frames = mutableListOf<ByteArray>()
        var pos = 0

        while (true) {
            // 1. Garbage: skip to the first STX, without needing a complete frame.
            while (pos < buffer.size && buffer[pos] != STX) pos++

            // 2. Need STX + LEN to know the size.
            if (buffer.size - pos < HEADER_SIZE) break

            // Big-endian LEN; `and 0xFF` avoids Byte sign extension.
            val len = ((buffer[pos + 1].toInt() and 0xFF) shl 8) or
                (buffer[pos + 2].toInt() and 0xFF)

            // 3. Absurd LEN: do not wait forever; this STX was a false one.
            if (len > MAX_LEN) {
                pos++
                continue
            }

            // 4. Complete frame? If not, wait for more data.
            val total = HEADER_SIZE + len + TRAILER_SIZE
            if (buffer.size - pos < total) break

            val etxIdx = pos + HEADER_SIZE + len
            val lrcIdx = etxIdx + 1

            // 5. Validation: ETX in place and correct LRC (XOR of LEN..ETX).
            val valid = buffer[etxIdx] == ETX &&
                lrc(pos + 1, etxIdx) == (buffer[lrcIdx].toInt() and 0xFF)

            if (valid) {
                frames += buffer.copyOfRange(pos + HEADER_SIZE, etxIdx)
                pos += total
            } else {
                // 6. Resync: drop ONLY this STX and look for the next one.
                pos++
            }
        }

        // Keep what was not consumed (partial frame) for the next feed().
        buffer = buffer.copyOfRange(pos, buffer.size)
        return frames
    }

    // XOR of buffer[from..to], both inclusive.
    private fun lrc(from: Int, to: Int): Int {
        var x = 0
        for (i in from..to) x = x xor (buffer[i].toInt() and 0xFF)
        return x
    }
}
