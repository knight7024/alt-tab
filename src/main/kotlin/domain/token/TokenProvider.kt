package com.example.domain.token

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.config.JwtConfig
import java.time.Clock
import java.time.Duration
import kotlin.random.Random

class TokenProvider(
    private val jwtConfig: JwtConfig,
    private val clock: Clock,
) {
    fun issueAll(tokenId: TokenId): Pair<AccessToken, RefreshToken> {
        val refreshToken = issueRefreshToken(tokenId)
        val accessToken = issueAccessToken(tokenId)

        return accessToken to refreshToken
    }

    private fun issueRefreshToken(tokenId: TokenId): RefreshToken {
        val issuedAt = clock.instant()
        val expiresIn = issuedAt.plus(RefreshToken.EXPIRES_IN)

        return JWT
            .create()
            .withIssuer(jwtConfig.issuer)
            .withJWTId(tokenId.pairingKey)
            .withSubject(tokenId.userId.value)
            .withIssuedAt(issuedAt)
            .sign(Algorithm.HMAC256(jwtConfig.secret))
            .let { RefreshToken(it, tokenId, expiresIn) }
    }

    private fun issueAccessToken(tokenId: TokenId): AccessToken {
        val expiresIn = clock.instant().plus(AccessToken.EXPIRES_IN)
        val jitter = Random.nextLong(1, 60).let { Duration.ofSeconds(it) }

        return JWT
            .create()
            .withIssuer(jwtConfig.issuer)
            .withExpiresAt(expiresIn.plus(jitter))
            .withJWTId(tokenId.pairingKey)
            .withSubject(tokenId.userId.value)
            .sign(Algorithm.HMAC256(jwtConfig.secret))
            .let { AccessToken(it) }
    }
}
