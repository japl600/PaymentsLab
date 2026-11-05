package com.japl.paymentslab.framing

import com.japl.paymentslab.util.toHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FrameBuilderTest {

    @Test
    fun `la trama de Hola coincide con el LRC calculado a mano`() {
        // LRC = 00 ^ 04 ^ 48 ^ 6F ^ 6C ^ 61 ^ 03 = 2D
        assertEquals("02 00 04 48 6F 6C 61 03 2D", FrameBuilder.build("Hola".toByteArray()).toHex())
    }

    @Test
    fun `badLrc cambia solo el ultimo byte`() {
        val good = FrameBuilder.build("Hola".toByteArray())
        val bad = FrameBuilder.build("Hola".toByteArray(), badLrc = true)

        assertEquals(good.dropLast(1), bad.dropLast(1))
        assertNotEquals(good.last(), bad.last())
    }
}
