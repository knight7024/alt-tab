package com.example.domain.token

import arrow.core.Either

interface RefreshTokenRepository {
    suspend fun save(token: RefreshToken)

    suspend fun invalidateOnce(token: RefreshToken): Either<InvalidateOnceError, Unit>

    sealed interface InvalidateOnceError {
        data object Purged : InvalidateOnceError

        data object Invalidated : InvalidateOnceError
    }

    suspend fun invalidateAll(tokenId: TokenId)
}
