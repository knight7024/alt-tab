package adapter

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.adapter.mongodb.RefreshTokenDocument
import com.example.domain.token.RefreshToken
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.RefreshTokenRepository.InvalidateOnceError
import com.example.domain.token.TokenId

class FakeRefreshTokenRepository : RefreshTokenRepository {
    private val refreshTokens = mutableMapOf<String, RefreshTokenDocument>()

    override suspend fun save(token: RefreshToken) {
        refreshTokens[token.value] = token.toDocument()
    }

    override suspend fun invalidateOnce(token: RefreshToken): Either<InvalidateOnceError, Unit> {
        val saved = refreshTokens[token.value]
        when {
            // 탈취된 토큰으로 판단되어 이미 제거된 경우
            saved == null -> return InvalidateOnceError.Purged.left()

            // 재사용할 수 없는 토큰인 경우
            saved.status != null -> return InvalidateOnceError.Invalidated.left()
        }

        refreshTokens[token.value] = saved!!.copy(status = "INVALIDATED")

        return Unit.right()
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
