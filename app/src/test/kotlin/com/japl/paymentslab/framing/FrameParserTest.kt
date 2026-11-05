package com.japl.paymentslab.framing

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FrameParserTest {

    private val hola = "Hola".toByteArray()
    private val mundo = "Mundo".toByteArray()

    @Test
    fun `una trama completa en un solo feed devuelve su DATA`() {
        val out = FrameParser().feed(FrameBuilder.build(hola))
        assertEquals(1, out.size)
        assertArrayEquals(hola, out.single())
    }

    @Test
    fun `una trama fragmentada byte a byte solo se entrega al completarse`() {
        val parser = FrameParser()
        val frame = FrameBuilder.build(hola)

        val intermediate = frame.dropLast(1).flatMap { parser.feed(byteArrayOf(it)) }
        val last = parser.feed(byteArrayOf(frame.last()))

        assertTrue(intermediate.isEmpty())
        assertArrayEquals(hola, last.single())
    }

    @Test
    fun `la basura antes del STX se descarta`() {
        val garbage = byteArrayOf(0x55, 0x10, 0x03, 0xFF.toByte())
        val out = FrameParser().feed(garbage + FrameBuilder.build(hola))
        assertArrayEquals(hola, out.single())
    }

    @Test
    fun `un LRC incorrecto descarta solo esa trama y sigue con la siguiente`() {
        val out = FrameParser().feed(FrameBuilder.build(hola, badLrc = true) + FrameBuilder.build(mundo))
        assertEquals(1, out.size)
        assertArrayEquals(mundo, out.single())
    }

    @Test
    fun `dos tramas en un mismo feed se devuelven en orden`() {
        val out = FrameParser().feed(FrameBuilder.build(hola) + FrameBuilder.build(mundo))
        assertEquals(2, out.size)
        assertArrayEquals(hola, out[0])
        assertArrayEquals(mundo, out[1])
    }

    @Test
    fun `un LEN corrupto que cubre la trama buena resincroniza y la recupera`() {
        // LEN says 20 but there are only 4 DATA bytes: the parser "eats" part of the next frame.
        val bad = byteArrayOf(0x02, 0x00, 20) + hola + 0x03.toByte() + 0x00.toByte()
        val out = FrameParser().feed(bad + FrameBuilder.build(mundo) + ByteArray(30))
        assertTrue(out.any { it.contentEquals(mundo) })
    }

    @Test
    fun `un LEN absurdo no bloquea al parser`() {
        val prefix = byteArrayOf(0x02, 0xFF.toByte(), 0xFF.toByte())
        val out = FrameParser().feed(prefix + FrameBuilder.build(hola))
        assertArrayEquals(hola, out.single())
    }

    @Test
    fun `un DATA que contiene STX y ETX no rompe la trama`() {
        val data = byteArrayOf(0x02, 0x03, 0x02, 0x03)
        val out = FrameParser().feed(FrameBuilder.build(data))
        assertArrayEquals(data, out.single())
    }

    @Test
    fun `un DATA vacio es una trama valida`() {
        val out = FrameParser().feed(FrameBuilder.build(ByteArray(0)))
        assertEquals(1, out.size)
        assertEquals(0, out.single().size)
    }

    @Test
    fun `los bytes mayores o iguales a 0x80 no rompen LEN ni LRC por el signo`() {
        val data = ByteArray(200) { (0x80 + it % 100).toByte() }
        val out = FrameParser().feed(FrameBuilder.build(data))
        assertArrayEquals(data, out.single())
    }
}
