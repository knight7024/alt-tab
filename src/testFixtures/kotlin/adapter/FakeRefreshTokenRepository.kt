package adapter

import com.example.adapter.mongodb.RefreshTokenDocument
import com.example.domain.token.RefreshToken
import com.example.domain.token.RefreshTokenRepository

class FakeRefreshTokenRepository : RefreshTokenRepository {
    private val refreshTokens = mutableMapOf<String, RefreshTokenDocument>()

    override suspend fun save(token: RefreshToken) {
        refreshTokens[token.value] = token.toDocument()
    }

    override suspend fun invalidateOnce(token: RefreshToken): Boolean {
        val saved = refreshTokens[token.value]
        if (saved == null) {
            return true
        } else if (saved.status != null) {
            return false
        }

        refreshTokens[token.value] = saved.copy(status = "INVALIDATED")

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
