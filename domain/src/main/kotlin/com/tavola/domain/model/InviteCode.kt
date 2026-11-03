package com.tavola.domain.model

import kotlin.random.Random

/**
 * The code that lets someone join a group: 8 characters from an alphabet without look-alikes (no 0, O, 1, I or L), about 10^12
 * combinations. It never expires; any member can renew it, and the old one stops working.
 */
object InviteCode {
    const val LENGTH = 8
    private const val ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    private const val GROUP_SIZE = 4

    /** A new code. [random] has to be a secure one: the code is the only thing that keeps strangers out of a group. */
    fun generate(random: Random): String = buildString(LENGTH) {
        repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) }
    }

    /** What the person typed ("k7m2-qx4r", with spaces or dashes) as a code, or `null` if it cannot be one. */
    fun parse(input: String): String? {
        val cleaned = input.uppercase().filter { character -> character.isLetterOrDigit() }
        return cleaned.takeIf { code -> code.length == LENGTH && code.all { character -> character in ALPHABET } }
    }

    /** The code as it is shown and shared: in two groups of four ("K7M2-QX4R"). */
    fun format(code: String): String = code.chunked(GROUP_SIZE).joinToString("-")
}

/** The rules for what a group is called and what color a member gets. */
object GroupRules {
    const val MAX_NAME_LENGTH = 40

    /** How many member colors the palette has; the screens wrap a member's color index around the colors they actually have. */
    const val MEMBER_COLOR_COUNT = 8

    /** The name as it is stored: line breaks become spaces, the ends are trimmed and it is cut at the limit; `null` if nothing is left. */
    fun cleanName(input: String): String? = input.replace(LINE_BREAKS, " ").trim().take(MAX_NAME_LENGTH).trim().ifEmpty { null }

    /** A color for a member that depends only on who they are, so it is the same on every phone and nobody has to coordinate. */
    fun memberColorIndex(userId: String): Int = (userId.hashCode() and Int.MAX_VALUE) % MEMBER_COLOR_COUNT

    private val LINE_BREAKS = Regex("\\s*[\\r\\n]+\\s*")
}
