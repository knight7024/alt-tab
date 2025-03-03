package com.example.domain.user

interface UserEmailRepository {
    suspend fun findByAccessToken(accessToken: String): String
}
