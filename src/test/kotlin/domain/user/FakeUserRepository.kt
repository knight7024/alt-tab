package com.example.domain.user

class FakeUserRepository : UserRepository {
    override suspend fun findByEmail(email: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun save(user: User) {
        TODO("Not yet implemented")
    }
}
