package com.example.adapter

import com.example.domain.token.RefreshToken
import com.example.domain.token.RefreshTokenRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class MongoRefreshTokenRepository(
    private val dao: MongoCollection<RefreshTokenDocument>,
) : RefreshTokenRepository {
    override suspend fun save(token: RefreshToken) {
        dao.insertOne(token.toDocument())
    }

    override suspend fun invalidateOnce(token: RefreshToken): Boolean {
        val saved =
            dao
                .find(
                    Filters.eq(RefreshTokenDocument.FIELD_VALUE, token.value),
                ).first()
        if (saved == null) {
            return true
        }

        return dao
            .updateOne(
                Filters.and(
                    Filters.eq(RefreshTokenDocument.FIELD_VALUE, token.value),
                    Filters.eq(RefreshTokenDocument.FIELD_STATUS, null),
                ),
                Updates.set(RefreshTokenDocument.FIELD_STATUS, "INVALIDATED"),
            ).modifiedCount != 0L
    }

    private fun RefreshToken.toDocument() =
        RefreshTokenDocument(
            userId = tokenId.userId.value,
            pairingKey = tokenId.pairingKey,
            expiresIn = expiresIn.toEpochMilli(),
            value = value,
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
    @SerialName(FIELD_STATUS)
    val status: String? = null,
) {
    companion object {
        const val FIELD_USER_ID = "user_id"
        const val FIELD_PAIRING_KEY = "pairing_key"
        const val FIELD_EXPIRES_IN = "expires_in"
        const val FIELD_VALUE = "value"
        const val FIELD_STATUS = "status"
    }
}
