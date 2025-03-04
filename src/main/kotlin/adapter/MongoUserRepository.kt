package com.example.adapter

import com.example.domain.user.User
import com.example.domain.user.UserId
import com.example.domain.user.UserRepository
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.kotlinx.ObjectIdSerializer
import org.bson.types.ObjectId
import java.time.Instant

class MongoUserRepository(
    private val dao: MongoCollection<UserDocument>,
) : UserRepository {
    override suspend fun findByEmail(email: String): User? =
        dao
            .find(
                Filters.eq(UserDocument.FIELD_EMAIL, email),
            ).firstOrNull()
            ?.toDomain()

    override suspend fun save(user: User) {
        dao.insertOne(user.toDocument())
    }

    private fun User.toDocument() =
        UserDocument(
            id = ObjectId(id.value),
            email = email,
            signedUpAt = signedUpAt.toEpochMilli(),
        )

    private fun UserDocument.toDomain() =
        User(
            id = UserId(id.toHexString()),
            email = email,
            signedUpAt = Instant.ofEpochMilli(signedUpAt),
        )
}

@Serializable
data class UserDocument(
    @SerialName(FIELD_ID)
    @OptIn(ExperimentalSerializationApi::class)
    @Serializable(with = ObjectIdSerializer::class)
    val id: ObjectId,
    @SerialName(FIELD_EMAIL)
    val email: String,
    @SerialName(FIELD_SIGNED_UP_AT)
    val signedUpAt: Long,
) {
    companion object {
        const val FIELD_ID = "_id"
        const val FIELD_EMAIL = "email"
        const val FIELD_SIGNED_UP_AT = "signed_up_at"
    }
}
