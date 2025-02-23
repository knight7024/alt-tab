package com.example.domain

interface UserRepository {
    suspend fun findByUuid(uuid: String): User?

    suspend fun save(user: User)
}
