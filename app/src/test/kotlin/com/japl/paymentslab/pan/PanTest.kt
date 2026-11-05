package com.japl.paymentslab.pan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PanTest {

    private fun assertMaskRejects(pan: String) {
        assertThrows(IllegalArgumentException::class.java) { maskPan(pan) }
    }

    @Test
    fun `maskPan conserva los seis primeros y los cuatro ultimos y la longitud`() {
        assertEquals("411111******1111", maskPan("4111111111111111"))
        assertEquals("378282*****0005", maskPan("378282246310005"))       // Amex, 15
        assertEquals("411111**1111", maskPan("411111111111"))             // minimum, 12
        assertEquals("411111*********1111", maskPan("4111111111111111111")) // maximum, 19
    }

    @Test
    fun `maskPan rechaza entradas invalidas sin filtrar el PAN en el mensaje`() {
        assertMaskRejects("")
        assertMaskRejects("41111111111")                 // 11, too short
        assertMaskRejects("41111111111111111111")        // 20, too long
        assertMaskRejects("4111abcd11111111")
        assertMaskRejects("４１１１１１１１１１１１１１１１")    // full-width digits

        val pan = "4111abcd11111111"
        val error = assertThrows(IllegalArgumentException::class.java) { maskPan(pan) }
        assertFalse(error.message.orEmpty().contains(pan))
    }

    @Test
    fun `isValidLuhn acepta tarjetas de prueba conocidas`() {
        assertTrue(isValidLuhn("4111111111111111"))   // Visa
        assertTrue(isValidLuhn("5500005555555559"))   // Mastercard
        assertTrue(isValidLuhn("378282246310005"))    // Amex
        assertTrue(isValidLuhn("6011111111111117"))   // Discover
    }

    @Test
    fun `isValidLuhn rechaza digito de control incorrecto y entradas mal formadas`() {
        assertFalse(isValidLuhn("4111111111111112"))
        assertFalse(isValidLuhn(""))
        assertFalse(isValidLuhn("4111-1111-1111-1111"))
        assertFalse(isValidLuhn("4111 1111 1111 1111"))
        assertFalse(isValidLuhn("4111"))
        assertFalse(isValidLuhn("41111111111111111111"))
        assertFalse(isValidLuhn("４１１１１１１１１１１１１１１１"))
    }
}
