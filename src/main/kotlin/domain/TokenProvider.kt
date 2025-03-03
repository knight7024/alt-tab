package com.example.domain

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config.JwtConfig
import java.time.Clock
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

class TokenProvider(
    private val jwtConfig: JwtConfig,
    private val clock: Clock,
) {
    fun issueAll(
        user: User,
        pairingKey: String = UUID.randomUUID().toString().replace("-", ""),
    ): Pair<AccessToken, RefreshToken> {
        val refreshToken = issueRefreshToken(user.uuid, pairingKey)
        val accessToken = issueAccessToken(user.uuid, pairingKey)

        return accessToken to refreshToken
    }

    private fun issueRefreshToken(
        userUuid: String,
        pairingKey: String,
    ): RefreshToken {
        val expiresIn = clock.instant().plus(REFRESH_TOKEN_EXPIRES_IN)

        return JWT
            .create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(expiresIn)
            .withJWTId(pairingKey)
            .withSubject(userUuid)
            .sign(Algorithm.HMAC256(jwtConfig.secret))
            .let { RefreshToken(it) }
    }

    private fun issueAccessToken(
        userUuid: String,
        pairingKey: String,
    ): AccessToken {
        val expiresIn = clock.instant().plus(ACCESS_TOKEN_EXPIRES_IN)
        val jitter = Random.nextLong(1, 60).let { Duration.ofSeconds(it) }

        return JWT
            .create()
            .withAudience(jwtConfig.audience)
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(expiresIn.plus(jitter))
            .withJWTId(pairingKey)
            .withSubject(userUuid)
            .sign(Algorithm.HMAC256(jwtConfig.secret))
            .let { AccessToken(it) }
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
