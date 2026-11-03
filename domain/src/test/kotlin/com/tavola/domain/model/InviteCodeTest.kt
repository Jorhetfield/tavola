package com.tavola.domain.model

import kotlin.random.Random
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class InviteCodeTest {
    @Test
    fun `a generated code has eight characters and none that look alike`() {
        val random = Random(seed = 7)

        repeat(200) {
            val code = InviteCode.generate(random)

            assertEquals(InviteCode.LENGTH, code.length)
            assertTrue(code.none { character -> character in "01OIL" }, "$code has a look-alike character")
            assertTrue(code.all { character -> character.isUpperCase() || character.isDigit() }, "$code is not upper case or digits")
        }
    }

    @Test
    fun `two generated codes are different`() {
        val random = Random(seed = 7)

        assertNotEquals(InviteCode.generate(random), InviteCode.generate(random))
    }

    @Test
    fun `a generated code is a valid code once parsed`() {
        val code = InviteCode.generate(Random(seed = 3))

        assertEquals(code, InviteCode.parse(code))
    }

    @Test
    fun `what the person types is cleaned up into the code`() {
        assertEquals("K7M2QX4R", InviteCode.parse("k7m2-qx4r"))
        assertEquals("K7M2QX4R", InviteCode.parse("  K7M2 QX4R \n"))
    }

    @Test
    fun `text that cannot be a code is rejected`() {
        assertNull(InviteCode.parse(""))
        assertNull(InviteCode.parse("K7M2QX4"))
        assertNull(InviteCode.parse("K7M2QX4RR"))
        // 0 and O look alike, so neither is in the alphabet.
        assertNull(InviteCode.parse("K7M2QX40"))
        assertNull(InviteCode.parse("K7M2QX4O"))
    }

    @Test
    fun `a code is shown in two groups of four`() {
        assertEquals("K7M2-QX4R", InviteCode.format("K7M2QX4R"))
    }
}
