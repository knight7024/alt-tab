package adapter

import com.example.adapter.UserDocument
import com.example.domain.user.User
import com.example.domain.user.UserId
import com.example.domain.user.UserRepository
import org.bson.types.ObjectId
import java.time.Clock
import java.time.Instant

class FakeUserRepository(
    private val clock: Clock,
) : UserRepository {
    private val users = mutableListOf<UserDocument>()

    override suspend fun find(userId: UserId): User? = users.find { it.id.toHexString() == userId.value }?.toDomain()

    override suspend fun findByEmail(email: String): User? = users.find { it.email == email }?.toDomain()

    override suspend fun signUp(email: String): User {
        val user =
            UserDocument(
                id = ObjectId(),
                email = email,
                signedUpAt = clock.millis(),
            )
        users.add(user)

        return user.toDomain()
    }

    private fun UserDocument.toDomain() =
        User(
            id = UserId(id.toHexString()),
            email = email,
            signedUpAt = Instant.ofEpochMilli(signedUpAt),
        )
}
