package com.tavola.domain.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GroupRulesTest {
    @Test
    fun `a name is trimmed and its line breaks become spaces`() {
        assertEquals("Casa de Marta", GroupRules.cleanName("  Casa de Marta  "))
        assertEquals("Piso Madrid", GroupRules.cleanName("Piso\nMadrid"))
    }

    @Test
    fun `a blank name is not a name`() {
        assertNull(GroupRules.cleanName(""))
        assertNull(GroupRules.cleanName("  \n  "))
    }

    @Test
    fun `a long name is cut at the limit`() {
        val name = GroupRules.cleanName("a".repeat(GroupRules.MAX_NAME_LENGTH + 20))

        assertEquals(GroupRules.MAX_NAME_LENGTH, name?.length)
    }

    @Test
    fun `a name is never cut in the middle of an emoji`() {
        val grinning = "😀"

        val name = GroupRules.cleanName("a".repeat(GroupRules.MAX_NAME_LENGTH - 1) + grinning)

        assertEquals("a".repeat(GroupRules.MAX_NAME_LENGTH - 1), name)
        // An emoji that fits is kept whole.
        assertEquals(
            "a".repeat(GroupRules.MAX_NAME_LENGTH - 2) + grinning,
            GroupRules.cleanName("a".repeat(GroupRules.MAX_NAME_LENGTH - 2) + grinning),
        )
    }

    @Test
    fun `the color of a member depends only on who they are and stays in the palette`() {
        val first = GroupRules.memberColorIndex("user-marta")

        assertEquals(first, GroupRules.memberColorIndex("user-marta"))
        listOf("user-marta", "user-luis", "", "a-much-longer-user-id-0123456789", "éñ").forEach { userId ->
            assertTrue(GroupRules.memberColorIndex(userId) in 0 until GroupRules.MEMBER_COLOR_COUNT, "$userId is out of the palette")
        }
    }
}
