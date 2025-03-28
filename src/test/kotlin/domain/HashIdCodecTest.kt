package com.example.domain

import com.example.clock
import com.example.hashIdCodec
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.StringSpec
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class HashIdCodecTest :
    StringSpec({
        "인코딩한 값과 디코딩한 값이 같은 경우" {
            val id = Random.nextLong(1, Long.MAX_VALUE)
            val expiresAt = clock.instant().plus(10, ChronoUnit.MINUTES)
            val encodedId = hashIdCodec.encode(id, expiresAt)
            val decodedId = hashIdCodec.decode(encodedId)
            decodedId shouldBeRight id
        }

        "임의의 값을 디코딩하려고 시도하는 경우" {
            val id = Random.nextLong(1, Long.MAX_VALUE - 1)
            val expiresAt = clock.instant().plus(10, ChronoUnit.MINUTES)
            val encodedId = hashIdCodec.encode(id, expiresAt)
            val decodedId = hashIdCodec.decode("$encodedId-_")
            decodedId shouldBeLeft Unit
        }

        "만료된 값을 디코딩하려고 시도하는 경우" {
            val id = Random.nextLong(1, Long.MAX_VALUE - 1)
            val expiresAt = clock.instant()
            val encodedId = hashIdCodec.encode(id, expiresAt)
            clock.tick(Duration.ofSeconds(1))

            val decodedId = hashIdCodec.decode(encodedId)
            decodedId shouldBeLeft Unit
        }
    })
