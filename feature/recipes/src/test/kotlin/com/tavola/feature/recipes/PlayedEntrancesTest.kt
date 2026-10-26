package com.tavola.feature.recipes

import com.tavola.core.designsystem.motion.PlayedEntrances
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** [PlayedEntrances] lives in the design system, which has no unit tests set up; it is exercised here where it is used. */
class PlayedEntrancesTest {
    @Test
    fun `a key is the first time only once`() {
        val played = PlayedEntrances()

        assertTrue(played.isFirstTime("recipe:paella"))
        assertFalse(played.isFirstTime("recipe:paella"))
    }

    @Test
    fun `different keys are independent`() {
        val played = PlayedEntrances()

        assertTrue(played.isFirstTime("recipe:paella"))
        assertTrue(played.isFirstTime("recipe:gazpacho"))
    }

    @Test
    fun `keys given at creation count as already played`() {
        val played = PlayedEntrances(listOf("recipe:paella"))

        assertFalse(played.isFirstTime("recipe:paella"))
        assertTrue(played.isFirstTime("recipe:tiramisu"))
    }
}
