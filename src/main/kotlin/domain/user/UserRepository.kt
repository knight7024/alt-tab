package com.example.domain.user

interface UserRepository {
    suspend fun find(userId: UserId): User?

    suspend fun findByEmail(email: String): User?

    suspend fun save(user: User)
}
