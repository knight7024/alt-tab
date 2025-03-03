package com.example.adapter

import com.example.domain.token.RefreshToken
import com.example.domain.token.RefreshTokenRepository
import com.example.domain.token.TokenId
import com.example.domain.user.UserId
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Clock
import java.time.Instant

class MongoRefreshTokenRepository(
    private val dao: MongoCollection<RefreshTokenDocument>,
    private val clock: Clock,
) : RefreshTokenRepository {
    override suspend fun findActiveByTokenId(tokenId: TokenId): Flow<RefreshToken> =
        dao
            .find(
                Filters.and(
                    Filters.eq(RefreshTokenDocument.FIELD_USER_ID, tokenId.userId.value),
                    Filters.eq(RefreshTokenDocument.FIELD_PAIRING_KEY, tokenId.pairingKey),
                    Filters.gt(RefreshTokenDocument.FIELD_EXPIRES_IN, clock.millis()),
                ),
            ).asFlow()
            .map { it.toDomain() }

    override suspend fun save(token: RefreshToken) {
        dao.insertOne(token.toDocument())
    }

    private fun RefreshToken.toDocument() =
        RefreshTokenDocument(
            userId = tokenId.userId.value,
            pairingKey = tokenId.pairingKey,
            expiresIn = expiresIn.toEpochMilli(),
            value = value,
        )

    private fun RefreshTokenDocument.toDomain() =
        RefreshToken(
            value = value,
            tokenId = TokenId(UserId(userId), pairingKey),
            expiresIn = Instant.ofEpochMilli(expiresIn),
        )
}

@Serializable
data class RefreshTokenDocument(
    @SerialName(FIELD_USER_ID)
    val userId: String,
    @SerialName(FIELD_PAIRING_KEY)
    val pairingKey: String,
    @SerialName(FIELD_EXPIRES_IN)
    val expiresIn: Long,
    @SerialName(FIELD_VALUE)
    val value: String,
) {
    companion object {
        const val FIELD_USER_ID = "user_id"
        const val FIELD_PAIRING_KEY = "pairing_key"
        const val FIELD_EXPIRES_IN = "expires_in"
        const val FIELD_VALUE = "value"
    }
}
