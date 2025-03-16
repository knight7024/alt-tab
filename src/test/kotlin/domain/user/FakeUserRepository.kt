package com.example.domain.user

import com.example.adapter.UserDocument
import org.bson.types.ObjectId
import java.time.Instant

class FakeUserRepository : UserRepository {
    private val users = mutableListOf<UserDocument>()

    override suspend fun findByEmail(email: String): User? = users.find { it.email == email }?.toDomain()

    override suspend fun save(user: User) {
        users.add(user.toDocument())
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
