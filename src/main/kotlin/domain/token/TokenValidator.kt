package com.example.domain.token

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.left
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.example.config.JwtConfig
import com.example.domain.user.UserId
import java.time.Clock

class TokenValidator(
    private val jwtConfig: JwtConfig,
    private val clock: Clock,
) {
    fun validate(
        accessToken: String,
        refreshToken: String,
    ): Either<Error, Pair<AccessToken, RefreshToken>> =
        runCatching {
            val decodedAccessToken = JWT.decode(accessToken)
            val decodedRefreshToken = JWT.decode(refreshToken)
            if (
                decodedAccessToken.id != decodedRefreshToken.id ||
                decodedAccessToken.subject != decodedRefreshToken.subject ||
                decodedAccessToken.issuedAt != decodedRefreshToken.issuedAt
            ) {
                return Error.Unpaired.left()
            }

            val tokenId =
                TokenId(
                    userId = UserId(decodedRefreshToken.subject),
                    pairingKey = decodedRefreshToken.id,
                )
            val accessTokenVerifier =
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.accessTokenSecret))
                    .withIssuer(jwtConfig.issuer)
                    .withJWTId(tokenId.pairingKey)
                    .withSubject(tokenId.userId.value)
                    .build()
            val refreshTokenVerifier =
                JWT
                    .require(Algorithm.HMAC256(jwtConfig.refreshTokenSecret))
                    .withIssuer(jwtConfig.issuer)
                    .withJWTId(tokenId.pairingKey)
                    .withSubject(tokenId.userId.value)
                    .build()

            val validRefreshToken =
                runCatching {
                    refreshTokenVerifier.verify(decodedRefreshToken)
                }.onFailure {
                    return Error.Invalid.left()
                }.map {
                    val expiresAt = it.issuedAtAsInstant + RefreshToken.EXPIRES_IN
                    if (expiresAt <= clock.instant()) {
                        return Error.Invalid.left()
                    }
                    RefreshToken(refreshToken, tokenId, expiresAt)
                }.getOrThrow()

            return catch {
                accessTokenVerifier.verify(decodedAccessToken)
            }.mapLeft {
                when (it) {
                    is TokenExpiredException -> Error.AccessTokenExpired(validRefreshToken)
                    else -> Error.Invalid
                }
            }.map {
                AccessToken(accessToken) to validRefreshToken
            }
        }.getOrElse {
            Error.Invalid.left()
        }

    sealed interface Error {
        data object Unpaired : Error

        data class AccessTokenExpired(
            val refreshToken: RefreshToken,
        ) : Error

        data object Invalid : Error
    }
}
