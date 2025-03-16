package com.example.domain.token

import com.example.adapter.RefreshTokenDocument

class FakeRefreshTokenRepository : RefreshTokenRepository {
    private val refreshTokens = mutableSetOf<RefreshTokenDocument>()

    override suspend fun save(token: RefreshToken) {
        refreshTokens.add(token.toDocument())
    }

    override suspend fun invalidateOnce(token: RefreshToken): Boolean {
        val saved = refreshTokens.find { it.value == token.value }
        if (saved == null || saved.status == "INVALIDATED") {
            return true
        }

        refreshTokens.remove(token.toDocument())
        refreshTokens.add(saved.copy(status = "INVALIDATED"))

        return true
    }

    private fun RefreshToken.toDocument() =
        RefreshTokenDocument(
            userId = tokenId.userId.value,
            pairingKey = tokenId.pairingKey,
            expiresIn = expiresIn.toEpochMilli(),
            value = value,
        )
}
