package com.example.domain.extension

import arrow.core.Either
import arrow.core.Either.Companion.catch
import org.sqids.Sqids
import org.sqids.Sqids.Builder.DEFAULT_ALPHABET
import java.time.Clock
import java.time.Instant

class HashIdCodec(
    private val clock: Clock,
) {
    fun encode(
        id: Long,
        expiresAt: Instant,
    ): String = squids.encode(listOf(id, expiresAt.epochSecond))

    // Decoding IDs will usually produce some kind of numeric output,
    // but that doesn't necessarily mean that the ID is canonical.
    // To check that the ID is valid, you can re-encode decoded numbers and check that the ID matches.
    fun decode(id: String): Either<Unit, Long> =
        catch {
            squids
                .decode(id)
                .also {
                    check(squids.encode(it) == id)
                    check(clock.millis() / 1000 < it[1])
                }.first()
        }.mapLeft { }

    companion object {
        private val squids =
            Sqids
                .builder()
                .minLength(4)
                .alphabet("$DEFAULT_ALPHABET-_")
                .build()
    }
}
