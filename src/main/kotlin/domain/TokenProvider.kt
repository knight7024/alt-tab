package com.example.domain

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Clock
import java.time.Duration
import kotlin.random.Random

class TokenProvider(
    private val jwtAudience: String,
    private val jwtIssuer: String,
    private val jwtSecret: String,
    private val clock: Clock,
) {
    fun issueAccessToken(user: User): AccessToken {
        val expiresIn = clock.instant().plus(ACCESS_TOKEN_EXPIRES_IN)
        val jitter = Random.nextLong(1, 60).let { Duration.ofSeconds(it) }

        return JWT
            .create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withExpiresAt(expiresIn.plus(jitter))
            .withClaim("uid", user.uuid)
            .sign(Algorithm.HMAC256(jwtSecret))
            .let { AccessToken(it) }
    }

    fun issueRefreshToken(user: User): RefreshToken {
        val expiresIn = clock.instant().plus(REFRESH_TOKEN_EXPIRES_IN)

        return JWT
            .create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withExpiresAt(expiresIn)
            .withClaim("uid", user.uuid)
            .sign(Algorithm.HMAC256(jwtSecret))
            .let { RefreshToken(it) }
    }

    companion object {
        private val ACCESS_TOKEN_EXPIRES_IN = Duration.ofMinutes(15)
        private val REFRESH_TOKEN_EXPIRES_IN = Duration.ofHours(6)
    }
}

@JvmInline
value class AccessToken internal constructor(
    val value: String,
)

@JvmInline
value class RefreshToken internal constructor(
    val value: String,
)
