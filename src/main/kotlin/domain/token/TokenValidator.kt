package com.example.domain.token

import arrow.core.Either
import arrow.core.left
import arrow.core.right
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
    ): Either<Error, TokenId> {
        val decodedAccessToken = JWT.decode(accessToken)
        val decodedRefreshToken = JWT.decode(refreshToken)
        if (
            decodedAccessToken.id != decodedRefreshToken.id ||
            decodedAccessToken.subject != decodedRefreshToken.subject
        ) {
            return Error.Unpaired.left()
        }

        val jwtVerifier =
            JWT
                .require(Algorithm.HMAC256(jwtConfig.secret))
                .withAudience(jwtConfig.audience)
                .withIssuer(jwtConfig.issuer)
                .withJWTId(decodedRefreshToken.id)
                .withSubject(decodedRefreshToken.subject)
                .build()

        runCatching {
            jwtVerifier.verify(decodedRefreshToken)
        }.onFailure {
            return Error.Invalid.left()
        }

        return runCatching {
            jwtVerifier
                .verify(decodedAccessToken)
                .let {
                    TokenId(
                        userId = UserId(it.subject),
                        pairingKey = it.id,
                    )
                }.right()
        }.getOrElse {
            when (it) {
                is TokenExpiredException -> {
                    Error
                        .Expired(
                            TokenId(
                                userId = UserId(decodedRefreshToken.subject),
                                pairingKey = decodedRefreshToken.id,
                            ),
                        ).left()
                }

                else -> {
                    Error.Invalid.left()
                }
            }
        }
    }

    sealed interface Error {
        data object Unpaired : Error

        data class Expired(
            val tokenId: TokenId,
        ) : Error

        data object Invalid : Error
    }
}
