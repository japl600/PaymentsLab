package com.japl.paymentslab.util

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HexTest {

    @Test
    fun `hexToBytes ignora espacios y acepta mayusculas y minusculas`() {
        assertArrayEquals(byteArrayOf(0x9F.toByte(), 0x26, 0xAB.toByte()), hexToBytes("9f 26\nAb"))
    }

    @Test
    fun `hexToBytes rechaza cantidad impar de digitos y caracteres no hexadecimales`() {
        assertThrows(IllegalArgumentException::class.java) { hexToBytes("9F2") }
        assertThrows(IllegalArgumentException::class.java) { hexToBytes("ZZ") }
    }

    @Test
    fun `toHex escribe mayusculas separadas por espacios y trata los bytes sin signo`() {
        assertEquals("9F 26 00 FF", byteArrayOf(0x9F.toByte(), 0x26, 0x00, 0xFF.toByte()).toHex())
    }
}
