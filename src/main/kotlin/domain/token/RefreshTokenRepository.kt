package com.example.domain.token

import kotlinx.coroutines.flow.Flow

interface RefreshTokenRepository {
    suspend fun findActiveByTokenId(tokenId: TokenId): Flow<RefreshToken>

    suspend fun save(token: RefreshToken)
}
