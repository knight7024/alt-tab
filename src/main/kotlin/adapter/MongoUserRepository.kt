package com.example.adapter

import com.example.domain.User
import com.example.domain.UserRepository
import com.mongodb.client.MongoCollection
import org.bson.Document
import java.time.Instant

class MongoUserRepository(
    private val dao: MongoCollection<UserDocument>,
) : UserRepository {
    override suspend fun findByUuid(uuid: String): User? =
        dao
            .find(UUID(uuid))
            .firstOrNull()
            ?.toDomain()

    override suspend fun save(user: User) {
        dao.insertOne(user.toDocument())
    }

    private fun User.toDocument() =
        UserDocument(
            uuid = uuid,
            signedUpAt = signedUpAt.toEpochMilli(),
        )

    private fun UserDocument.toDomain() =
        User(
            uuid = uuid,
            signedUpAt = Instant.ofEpochMilli(signedUpAt),
        )

    private companion object {
        val UUID: (String) -> Document = { Document(UserDocument.FIELD_UUID, it) }
    }
}
