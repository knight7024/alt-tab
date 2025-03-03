package com.example.domain.user

interface UserRepository {
    suspend fun findByUuid(uuid: String): User?

    suspend fun save(user: User)
}
