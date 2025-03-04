package com.example.domain.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config.JwtConfig
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.random.Random

class TokenProvider(
    private val jwtConfig: JwtConfig,
    private val clock: Clock,
) {
    fun issueAll(tokenId: TokenId): Pair<AccessToken, RefreshToken> {
        val issuedAt = clock.instant()
        val refreshToken = issueRefreshToken(tokenId, issuedAt)
        val accessToken = issueAccessToken(tokenId, issuedAt)

        return accessToken to refreshToken
    }

    private fun issueRefreshToken(
        tokenId: TokenId,
        issuedAt: Instant,
    ): RefreshToken {
        val expiresIn = issuedAt.plus(RefreshToken.EXPIRES_IN)

        return JWT
            .create()
            .withIssuer(jwtConfig.issuer)
            .withIssuedAt(issuedAt)
            .withJWTId(tokenId.pairingKey)
            .withSubject(tokenId.userId.value)
            .sign(Algorithm.HMAC256(jwtConfig.refreshTokenSecret))
            .let { RefreshToken(it, tokenId, expiresIn) }
    }

    private fun issueAccessToken(
        tokenId: TokenId,
        issuedAt: Instant,
    ): AccessToken {
        val expiresIn = issuedAt.plus(AccessToken.EXPIRES_IN)
        val jitter = Random.nextLong(1, 60).let { Duration.ofSeconds(it) }

        return JWT
            .create()
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(expiresIn.plus(jitter))
            .withIssuedAt(issuedAt)
            .withJWTId(tokenId.pairingKey)
            .withSubject(tokenId.userId.value)
            .sign(Algorithm.HMAC256(jwtConfig.accessTokenSecret))
            .let { AccessToken(it) }
    }
}
