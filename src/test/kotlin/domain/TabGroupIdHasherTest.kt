package com.example.domain

import com.example.clock
import com.example.route.extension.TabGroupId
import com.example.route.extension.TabGroupIdHasher
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class TabGroupIdHasherTest :
    StringSpec({
        "인코딩한 값과 디코딩한 값이 같은 경우" {
            val id = Random.nextLong(1, Long.MAX_VALUE)
            val expiresAt = clock.instant().plus(10, ChronoUnit.MINUTES)
            val encodedId = TabGroupIdHasher.encode(TabGroupId.Expiring(id, expiresAt))
            val decodedId = TabGroupIdHasher.decode(encodedId)
            decodedId shouldBe TabGroupId.Expiring(id, expiresAt.truncatedTo(ChronoUnit.SECONDS))
        }

        "임의의 값을 디코딩하려고 시도하는 경우" {
            val id = Random.nextLong(1, Long.MAX_VALUE - 1)
            val expiresAt = clock.instant().plus(10, ChronoUnit.MINUTES)
            val encodedId = TabGroupIdHasher.encode(TabGroupId.Expiring(id, expiresAt))
            shouldThrow<IllegalStateException> {
                TabGroupIdHasher.decode("$encodedId-_")
            }
        }
    })
