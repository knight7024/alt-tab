package com.example.domain.extension

import org.sqids.DEFAULT_ALPHABET
import org.sqids.Sqids
import java.time.Instant

object TabGroupIdHasher {
    fun encode(id: TabGroupId): String =
        when (id) {
            is TabGroupId.Expiring -> squids.encode(listOf(id.numeric, id.expiresAt.epochSecond))
            is TabGroupId.Persistent -> squids.encode(listOf(id.numeric))
        }

    // Decoding IDs will usually produce some kind of numeric output,
    // but that doesn't necessarily mean that the ID is canonical.
    // To check that the ID is valid, you can re-encode decoded numbers and check that the ID matches.
    fun decode(id: String): TabGroupId =
        squids
            .decode(id)
            .also { check(squids.encode(it) == id) }
            .let {
                when (it.size) {
                    1 -> TabGroupId.Persistent(it.first())
                    2 -> TabGroupId.Expiring(it[0], Instant.ofEpochSecond(it[1]))
                    else -> error("invalid id found: $id")
                }
            }

    private val squids = Sqids(minLength = 4, alphabet = "$DEFAULT_ALPHABET-_")
}

sealed interface TabGroupId {
    val numeric: Long

    data class Persistent(
        override val numeric: Long,
    ) : TabGroupId

    data class Expiring(
        override val numeric: Long,
        val expiresAt: Instant,
    ) : TabGroupId
}
