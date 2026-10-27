package com.tavola.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/** Reads and writes the JSON text columns of the Room entities. */
internal object JsonColumns {
    private val json = Json { ignoreUnknownKeys = true }

    fun <T> encode(serializer: KSerializer<T>, value: T): String = json.encodeToString(serializer, value)

    /**
     * Decodes [text], or returns [fallback] if it is malformed. One damaged column must not make every screen that
     * shows that row fail for good, so the row is read with the fallback instead.
     */
    fun <T> decodeOrElse(serializer: KSerializer<T>, text: String, fallback: T): T = try {
        json.decodeFromString(serializer, text)
    } catch (malformed: IllegalArgumentException) {
        // SerializationException (including JsonDecodingException) is an IllegalArgumentException.
        fallback
    }
}
