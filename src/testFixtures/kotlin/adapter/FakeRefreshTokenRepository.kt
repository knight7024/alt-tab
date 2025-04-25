package adapter

import com.example.adapter.mongodb.RefreshTokenDocument
import com.example.domain.token.RefreshToken
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenId

class FakeRefreshTokenRepository : RefreshTokenRepository {
    private val refreshTokens = mutableMapOf<String, RefreshTokenDocument>()

    override suspend fun save(token: RefreshToken) {
        refreshTokens[token.value] = token.toDocument()
    }

    override suspend fun invalidateOnce(token: RefreshToken): Boolean {
        val saved = refreshTokens[token.value]
        if (saved == null || saved.status != null) {
            return false
        }

        refreshTokens[token.value] = saved.copy(status = "INVALIDATED")

        return true
    }

    override suspend fun invalidateAll(tokenId: TokenId) {
        refreshTokens.values.removeIf {
            it.userId == tokenId.userId.value &&
                it.pairingKey == tokenId.pairingKey
        }
    }

    private fun RefreshToken.toDocument() =
        RefreshTokenDocument(
            userId = tokenId.userId.value,
            pairingKey = tokenId.pairingKey,
            expiresIn = expiresIn.toEpochMilli(),
            value = value,
        )
}
