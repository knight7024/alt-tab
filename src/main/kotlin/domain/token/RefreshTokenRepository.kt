package com.example.domain.token

interface RefreshTokenRepository {
    suspend fun save(token: RefreshToken)

    suspend fun invalidateOnce(token: RefreshToken): Boolean
}
