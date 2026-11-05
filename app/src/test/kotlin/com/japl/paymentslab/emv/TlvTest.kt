package com.japl.paymentslab.emv

import com.japl.paymentslab.util.hexToBytes
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TlvTest {

    private fun parse(hex: String) = parseTlv(hexToBytes(hex))

    private fun assertRejected(hex: String) {
        assertThrows(IllegalArgumentException::class.java) { parse(hex) }
    }

    @Test
    fun `el vector del enunciado devuelve tres TLV`() {
        val result = parse("9F2608 1122334455667788 9F2701 80 9505 0000008000")

        assertEquals(listOf("9F26", "9F27", "95"), result.map { it.tag })
        assertEquals(listOf(8, 1, 5), result.map { it.value.size })
        assertArrayEquals(hexToBytes("1122334455667788"), result[0].value)
        assertArrayEquals(hexToBytes("80"), result[1].value)
        assertArrayEquals(hexToBytes("0000008000"), result[2].value)
    }

    @Test
    fun `un tag de tres bytes se devuelve en hexadecimal mayuscula`() {
        val tlv = parse("DF8101 02 AABB").single()

        assertEquals("DF8101", tlv.tag)
        assertArrayEquals(hexToBytes("AABB"), tlv.value)
    }

    @Test
    fun `las longitudes 0x81 y 0x82 se decodifican`() {
        assertEquals(128, parseTlv(hexToBytes("9F26 81 80") + ByteArray(128)).single().value.size)
        assertEquals(256, parseTlv(hexToBytes("9F26 82 0100") + ByteArray(256)).single().value.size)
    }

    @Test
    fun `una entrada vacia devuelve una lista vacia`() {
        assertTrue(parseTlv(ByteArray(0)).isEmpty())
    }

    @Test
    fun `un tag truncado lanza IllegalArgumentException`() {
        assertRejected("9F")       // second tag byte missing
        assertRejected("9F A1")    // high bit asks for another byte and there is none
    }

    @Test
    fun `una longitud ausente o truncada lanza IllegalArgumentException`() {
        assertRejected("95")           // no length
        assertRejected("95 81")        // 0x81 without its byte
        assertRejected("95 82 01")     // 0x82 with a single byte
    }

    @Test
    fun `un valor truncado lanza IllegalArgumentException`() {
        assertRejected("95 05 0000")
    }

    @Test
    fun `las formas de longitud no soportadas se rechazan`() {
        assertRejected("95 80")                // indefinite
        assertRejected("95 83 000001 00")      // 3 length bytes
    }

    @Test
    fun `un tag constructed expone sus hijos`() {
        val tlv = parse("77 04 9F2701 80").single()

        assertTrue(tlv.isConstructed())
        assertEquals(listOf(Tlv("9F27", hexToBytes("80"))), tlv.children())
    }

    @Test
    fun `un tag primitivo no tiene hijos`() {
        val tlv = parse("95 01 20").single()

        assertFalse(tlv.isConstructed())
        assertTrue(tlv.children().isEmpty())
    }

    @Test
    fun `un hijo truncado propaga IllegalArgumentException`() {
        val tlv = parse("77 03 9F2701").single()

        assertThrows(IllegalArgumentException::class.java) { tlv.children() }
    }
}
