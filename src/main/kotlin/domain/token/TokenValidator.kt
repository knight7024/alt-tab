package com.example.domain.token

import arrow.core.Either
import arrow.core.Either.Companion.catch
import arrow.core.left
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.example.config.JwtConfig
import com.example.domain.user.UserId

class TokenValidator(
    private val jwtConfig: JwtConfig,
) {
    fun validate(
        accessToken: String,
        refreshToken: String,
    ): Either<Error, Pair<AccessToken, RefreshToken>> {
        val decodedAccessToken = JWT.decode(accessToken)
        val decodedRefreshToken = JWT.decode(refreshToken)
        if (
            decodedAccessToken.id != decodedRefreshToken.id ||
            decodedAccessToken.subject != decodedRefreshToken.subject
        ) {
            return Error.Unpaired.left()
        }

        val tokenId =
            TokenId(
                userId = UserId(decodedRefreshToken.subject),
                pairingKey = decodedRefreshToken.id,
            )
        val jwtVerifier =
            JWT
                .require(Algorithm.HMAC256(jwtConfig.secret))
                .withAudience(jwtConfig.audience)
                .withIssuer(jwtConfig.issuer)
                .withJWTId(tokenId.pairingKey)
                .withSubject(tokenId.userId.value)
                .build()

        runCatching {
            jwtVerifier.verify(decodedRefreshToken)
        }.onFailure {
            return Error.Invalid.left()
        }

        return catch {
            jwtVerifier.verify(decodedAccessToken)
        }.mapLeft {
            when (it) {
                is TokenExpiredException -> {
                    Error.AccessTokenExpired(
                        RefreshToken(
                            refreshToken,
                            tokenId,
                            decodedRefreshToken.expiresAtAsInstant,
                        ),
                    )
                }

                else -> {
                    Error.Invalid
                }
            }
        }.map {
            AccessToken(accessToken) to RefreshToken(refreshToken, tokenId, decodedRefreshToken.expiresAtAsInstant)
        }
    }

    sealed interface Error {
        data object Unpaired : Error

        data class AccessTokenExpired(
            val refreshToken: RefreshToken,
        ) : Error

        data object Invalid : Error
    }
}
